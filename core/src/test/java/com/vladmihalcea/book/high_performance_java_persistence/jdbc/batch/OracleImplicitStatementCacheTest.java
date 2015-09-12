package com.vladmihalcea.book.high_performance_java_persistence.jdbc.batch;

import com.vladmihalcea.book.high_performance_java_persistence.jdbc.batch.providers.BatchEntityProvider;
import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractOracleXEIntegrationTest;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.pool.OracleDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * OracleImplicitStatementCacheTest - Test Oracle implicit Statement cache
 *
 * @author Vlad Mihalcea
 */
public class OracleImplicitStatementCacheTest extends AbstractOracleXEIntegrationTest {

    public static final String INSERT_POST = "insert into Post (title, version, id) values ('Post no. %1$d', 0, %1$d)";

    private BatchEntityProvider entityProvider = new BatchEntityProvider();

    @Override
    protected DataSourceProvider getDataSourceProvider() {
        return new OracleDataSourceProvider() {
            @Override
            public DataSource dataSource() {
                OracleDataSource dataSource = (OracleDataSource) super.dataSource();
                try {
                    Properties connectionProperties = dataSource.getConnectionProperties();
                    if(connectionProperties == null) {
                        connectionProperties = new Properties();
                    }
                    connectionProperties.put("oracle.jdbc.implicitStatementCacheSize", "5");
                    dataSource.setConnectionProperties(connectionProperties);
                } catch (SQLException e) {
                    fail(e.getMessage());
                }
                return dataSource;
            }
        };
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
            OracleConnection oracleConnection = (OracleConnection) connection;
            assertTrue(oracleConnection.getImplicitCachingEnabled());
            assertEquals(5, oracleConnection.getStatementCacheSize());
            try (Statement statement = connection.createStatement()) {
                int postCount = getPostCount();

                for(int i = 0; i < postCount; i++) {
                    statement.executeUpdate(String.format(INSERT_POST, i));
                }
            } catch (SQLException e) {
                fail(e.getMessage());
            }
            for(int i = 0; i < 5; i++) {
                try (
                        PreparedStatement statement = connection.prepareStatement("select count(*) from post where id > ?");
                ) {
                    OraclePreparedStatement oraclePreparedStatement = (OraclePreparedStatement) statement;
                    assertTrue(statement.isPoolable());
                }
            }
        });
        LOGGER.info("{}.testInsert for {} took {} millis",
                getClass().getSimpleName(),
                getDataSourceProvider().getClass().getSimpleName(),
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos));
    }

    protected int getPostCount() {
        return 1000;
    }

    @Override
    protected boolean proxyDataSource() {
        return false;
    }
}
