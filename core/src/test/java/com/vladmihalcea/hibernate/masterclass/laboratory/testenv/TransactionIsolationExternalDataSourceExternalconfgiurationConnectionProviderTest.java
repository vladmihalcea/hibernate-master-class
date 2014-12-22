package com.vladmihalcea.hibernate.masterclass.laboratory.testenv;

import net.ttddyy.dsproxy.listener.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Environment;
import org.hibernate.jdbc.Work;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class TransactionIsolationExternalDataSourceExternalconfgiurationConnectionProviderTest extends ExternalDataSourceConnectionProviderTest {

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

    protected ProxyDataSource newDataSource() {
        JDBCDataSource actualDataSource = new JDBCDataSource();
        actualDataSource.setUrl("jdbc:hsqldb:mem:test");
        actualDataSource.setUser("sa");
        actualDataSource.setPassword("");
        Properties properties = new Properties();
        properties.setProperty("hsqldb.tx_level", "SERIALIZABLE");
        actualDataSource.setProperties(properties);
        ProxyDataSource proxyDataSource = new ProxyDataSource();
        proxyDataSource.setDataSource(actualDataSource);
        proxyDataSource.setListener(new SLF4JQueryLoggingListener());
        return proxyDataSource;
    }
}
