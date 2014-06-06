package com.vladmihalcea;

import net.ttddyy.dsproxy.listener.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Test;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.sql.DataSource;
import java.util.Properties;

public class ExternalDataSourceConnectionProviderTest {

    @Test
    public void test() {
        final DataSource dataSource = newDataSource();
        final SessionFactory sf = buildSessionFactory(dataSource);
        Session session = null;
        Transaction txn = null;
        try {
            session = sf.openSession();
            txn = session.beginTransaction();

            SecurityId securityId = new SecurityId();
            session.persist(securityId);

            txn.commit();
        } catch (RuntimeException e) {
            if ( txn != null && txn.isActive() ) txn.rollback();
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
            sf.close();
        }
    }

    private ProxyDataSource newDataSource() {
        JDBCDataSource actualDataSource = new JDBCDataSource();
        actualDataSource.setUrl("jdbc:hsqldb:mem:test");
        actualDataSource.setUser("sa");
        actualDataSource.setPassword("");
        ProxyDataSource proxyDataSource = new ProxyDataSource();
        proxyDataSource.setDataSource(actualDataSource);
        proxyDataSource.setListener(new SLF4JQueryLoggingListener());
        return proxyDataSource;
    }

    private SessionFactory buildSessionFactory(DataSource dataSource) {
        Properties properties = new Properties();
        properties.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        //log settings
        properties.put("hibernate.hbm2ddl.auto", "update");
        //data source settings
        properties.put("hibernate.connection.datasource", dataSource);

        return new Configuration()
                .addProperties(properties)
                .addAnnotatedClass(SecurityId.class)
                .buildSessionFactory(
                        new StandardServiceRegistryBuilder()
                                .applySettings(properties)
                                .build()
        );
    }

    @Entity(name = "SecurityId")
    private static class SecurityId {
        @Id
        @GeneratedValue
        Long id;
    }
}
