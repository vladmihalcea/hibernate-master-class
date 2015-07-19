package com.vladmihalcea.book.high_performance_java_persistence.jdbc.batch;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.DataSourceProviderIntegrationTest;
import org.junit.Test;

import javax.persistence.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;

/**
 * AbstractBatchPreparedStatementTest - Base class for testing JDBC PreparedStatement  batching
 *
 * @author Vlad Mihalcea
 */
public abstract class AbstractBatchPreparedStatementTest extends DataSourceProviderIntegrationTest {

    public static final String INSERT_POST = "insert into Post (title, version, id) values (?, ?, ?)";

    public static final String INSERT_POST_COMMENT = "insert into PostComment (post_id, review, version, id) values (?, ?, ?, ?)";

    private BatchEntityProvider entityProvider = new BatchEntityProvider();

    public AbstractBatchPreparedStatementTest(DataSourceProvider dataSourceProvider) {
        super(dataSourceProvider);
    }

    @Override
    protected Class<?>[] entities() {
        return entityProvider.entities();
    }

    @Test
    public void testInsert() {
        LOGGER.info("Test batch insert");
        long startNanos = System.nanoTime();
        doInConnection(connection -> {
            try (PreparedStatement postStatement = connection.prepareStatement(INSERT_POST);
                 PreparedStatement postCommentStatement = connection.prepareStatement(INSERT_POST_COMMENT)) {
                int postCount = getPostCount();
                int postCommentCount = getPostCommentCount();

                for(int i = 0; i < postCount; i++) {
                    int index = 0;
                    postStatement.setString(++index, String.format("Post no. %1$d", i));
                    postStatement.setInt(++index, 0);
                    postStatement.setLong(++index, i);
                    onStatement(postStatement);
                    for(int j = 0; j < postCommentCount; j++) {
                        index = 0;

                        postCommentStatement.setLong(++index, i);
                        postCommentStatement.setString(++index, String.format("Post comment %1$d", j));
                        postCommentStatement.setInt(++index, 0);
                        postCommentStatement.setLong(++index, (postCommentCount * i) + j);
                        onStatement(postCommentStatement);
                        int insertCount = (i * (1 + postCommentCount)) + (2 + j);
                        if(insertCount % getBatchSize() == 0) {
                            onFlush(postStatement);
                            onFlush(postCommentStatement);
                        }
                    }
                }
                onEnd(postStatement);
                onEnd(postCommentStatement);
            } catch (SQLException e) {
                fail(e.getMessage());
            }
        });
        LOGGER.info("{}.testInsert for {} took {} millis",
                getClass().getSimpleName(),
                getDataSourceProvider().getClass().getSimpleName(),
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos));
    }

    protected abstract void onFlush(PreparedStatement statement) throws SQLException;

    protected abstract void onStatement(PreparedStatement statement) throws SQLException;

    protected abstract void onEnd(PreparedStatement statement) throws SQLException;

    protected int getPostCount() {
        return 1000;
    }

    protected int getPostCommentCount() {
        return 5;
    }

    protected int getBatchSize() {
        return 1;
    }
}
