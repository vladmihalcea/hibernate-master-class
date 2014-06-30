package com.vladmihalcea.hibernate.masterclass.laboratory.util;

import net.ttddyy.dsproxy.listener.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public abstract class AbstractTest {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    protected static abstract class TransactionCallable<T> {
        public abstract T execute(Session session);
    }

    private SessionFactory sf;

    @Before
    public void init() {
        sf = newSessionFactory();
    }

    @After
    public void destroy() {
        sf.close();
    }

    protected abstract Class<?>[] entities();

    private SessionFactory newSessionFactory() {
        Properties properties = getProperties();
        Configuration configuration = new Configuration().addProperties(properties);
        for(Class<?> entityClass : entities()) {
            configuration.addAnnotatedClass(entityClass);
        }
        return configuration.buildSessionFactory(
                new StandardServiceRegistryBuilder()
                        .applySettings(properties)
                        .build()
        );
    }

    protected Properties getProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        //log settings
        properties.put("hibernate.hbm2ddl.auto", "update");
        //data source settings
        properties.put("hibernate.connection.datasource", newDataSource());
        return properties;
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

    protected <T> T doInTransaction(TransactionCallable<T> callable) {
        T result = null;
        Session session = null;
        Transaction txn = null;
        try {
            session = sf.openSession();
            txn = session.beginTransaction();

            result = callable.execute(session);
            txn.commit();
        } catch (RuntimeException e) {
            if ( txn != null && txn.isActive() ) txn.rollback();
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return result;
    }
}
