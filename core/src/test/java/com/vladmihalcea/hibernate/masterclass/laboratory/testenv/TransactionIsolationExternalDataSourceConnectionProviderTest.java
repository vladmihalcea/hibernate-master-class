package com.vladmihalcea.hibernate.masterclass.laboratory.testenv;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Environment;
import org.hibernate.jdbc.Work;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class TransactionIsolationExternalDataSourceConnectionProviderTest extends ExternalDataSourceConnectionProviderTest {

    @Override
    protected Properties getProperties() {
        Properties properties = super.getProperties();
        properties.setProperty("hibernate.connection.isolation", String.valueOf(Connection.TRANSACTION_SERIALIZABLE));
        return properties;
    }

    @Test
    public void test() {
        Session session = null;
        Transaction txn = null;
        try {
            session = getSessionFactory().openSession();
            txn = session.beginTransaction();
            session.doWork(new Work() {
                @Override
                public void execute(Connection connection) throws SQLException {
                    LOGGER.debug("Transaction isolation level is {}", Environment.isolationLevelToString(connection.getTransactionIsolation()));
                }
            });
            txn.commit();
        } catch (RuntimeException e) {
            if ( txn != null && txn.isActive() ) txn.rollback();
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
