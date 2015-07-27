package com.vladmihalcea.book.high_performance_java_persistence.jdbc.batch;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.DataSourceProviderIntegrationTest;
import org.junit.Test;

import java.sql.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GeneratedKeysBatchPreparedStatementTest - Base class for testing JDBC PreparedStatement generated keys
 *
 * @author Vlad Mihalcea
 */
public class GeneratedKeysBatchPreparedStatementTest extends DataSourceProviderIntegrationTest {

    private AutoIncrementBatchEntityProvider entityProvider = new AutoIncrementBatchEntityProvider();

    public GeneratedKeysBatchPreparedStatementTest(DataSourceProvider dataSourceProvider) {
        super(dataSourceProvider);
    }

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
        if(getDataSourceProvider().database() == DataSourceProvider.Database.ORACLE) {
            LOGGER.info("Oracle doesn't support getGeneratedKeys while using batch updates");
            return;
        }

        AtomicInteger postStatementCount = new AtomicInteger();

        try(PreparedStatement postStatement = connection.prepareStatement(String.format("insert into Post (id, title, version) values (%s, ?, ?)", identifier()), Statement.RETURN_GENERATED_KEYS)) {
            int postCount = getPostCount();

            int index;

            for (int i = 0; i < postCount; i++) {
                index = 0;

                postStatement.setString(++index, String.format("Post no. %1$d", i));
                postStatement.setInt(++index, 0);
                postStatement.addBatch();
                int count = postStatementCount.incrementAndGet();
                if(count % getBatchSize() == 0) {
                    postStatement.executeBatch();
                    try(ResultSet resultSet = postStatement.getGeneratedKeys()) {
                        while (resultSet.next()) {
                            LOGGER.info("Generated identifier: {}", resultSet.getLong(1));
                        }
                    }
                }
            }
            LOGGER.info("Done");
        }
    }

    private String identifier() {
        DataSourceProvider dataSourceProvider = getDataSourceProvider();
        if(dataSourceProvider.identifierStrategies().contains(DataSourceProvider.IdentifierStrategy.IDENTITY)) {
            return "DEFAULT";
        } else if(dataSourceProvider.database() == DataSourceProvider.Database.ORACLE) {
            return "hibernate_sequence.NEXTVAL";
        } else if(dataSourceProvider.database() == DataSourceProvider.Database.POSTGRESQL) {
            return "nextval('hibernate_sequence')";
        }
        throw new UnsupportedOperationException("Unsupported identifier for data provider " + dataSourceProvider);
    }


}
