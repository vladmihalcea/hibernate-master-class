package com.vladmihalcea.book.high_performance_java_persistence.jdbc.batch.generatedkeys.sequence;

import com.vladmihalcea.book.high_performance_java_persistence.jdbc.batch.providers.SequenceBatchEntityProvider;
import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractOracleXEIntegrationTest;
import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.junit.Test;

import java.sql.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AbstractSequenceGeneratedKeysBatchPreparedStatementTest - Base class for testing JDBC PreparedStatement generated keys for Sequences
 *
 * @author Vlad Mihalcea
 */
public abstract class AbstractSequenceGeneratedKeysBatchPreparedStatementTest extends AbstractTest {

    private SequenceBatchEntityProvider entityProvider = new SequenceBatchEntityProvider();

    @Override
    protected Class<?>[] entities() {
        return entityProvider.entities();
    }

    @Test
    public void testBatch() {
        doInConnection(this::batchInsert);
    }

    protected int getPostCount() {
        return 10;
    }

    protected int getBatchSize() {
        return 5;
    }

    protected void batchInsert(Connection connection) throws SQLException {
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        LOGGER.info("{} Driver supportsGetGeneratedKeys: {}", getDataSourceProvider().database(), databaseMetaData.supportsGetGeneratedKeys());

        AtomicInteger postStatementCount = new AtomicInteger();

        try(PreparedStatement postStatement = connection.prepareStatement("insert into Post (id, title, version) values (?, ?, ?)")) {
            int postCount = getPostCount();

            int index;

            for (int i = 0; i < postCount; i++) {
                index = 0;

                postStatement.setLong(++index, nextSequence(connection));
                postStatement.setString(++index, String.format("Post no. %1$d", i));
                postStatement.setInt(++index, 0);
                postStatement.addBatch();
                int count = postStatementCount.incrementAndGet();
                if(count % getBatchSize() == 0) {
                    postStatement.executeBatch();
                }
            }
        }
    }

    protected long nextSequence(Connection connection) throws SQLException {
        try(Statement statement = connection.createStatement()) {
            try(ResultSet resultSet = statement.executeQuery(callSequenceSyntax())) {
                resultSet.next();
                long id = resultSet.getLong(1);
                LOGGER.info("Generated id {}", id);
                return id;
            }
        }
    }

    protected abstract String callSequenceSyntax();
}
