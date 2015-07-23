package com.vladmihalcea.book.high_performance_java_persistence.jdbc.batch;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractPostgreSQLIntegrationTest;
import org.junit.Test;

import java.sql.Statement;

import static org.junit.Assert.assertEquals;

/**
 * <code>SimpleBatchStatementTest</code> - Simple Batch StatementTest
 *
 * @author Vlad Mihalcea
 */
public class SimpleBatchTest extends AbstractPostgreSQLIntegrationTest {

    private BatchEntityProvider batchEntityProvider = new BatchEntityProvider();

    @Override
    protected Class<?>[] entities() {
        return batchEntityProvider.entities();
    }

    @Test
    public void testStatement() {
        LOGGER.info("Test Statement batch insert");
        doInConnection(connection -> {
            try (Statement statement = connection.createStatement()) {

                statement.addBatch(
                    "insert into Post (title, version, id) " +
                    "values ('Post no. 1', 0, 1)");

                statement.addBatch(
                    "insert into PostComment (post_id, review, version, id) " +
                    "values (1, 'Post comment 1.1', 0, 1)");
                statement.addBatch(
                    "insert into PostComment (post_id, review, version, id) " +
                    "values (1, 'Post comment 1.2', 0, 2)");

                int[] updateCounts = statement.executeBatch();

                assertEquals(4, updateCounts.length);
            }
        });
    }
}
