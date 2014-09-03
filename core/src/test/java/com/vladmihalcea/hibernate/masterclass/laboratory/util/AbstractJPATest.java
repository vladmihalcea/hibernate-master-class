package com.vladmihalcea.hibernate.masterclass.laboratory.util;

import net.ttddyy.dsproxy.listener.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.Arrays;
import java.util.Properties;

public abstract class AbstractJPATest {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    protected static abstract class TransactionCallable<T> {
        public abstract T execute(EntityManager entityManager);
    }

    private EntityManagerFactory entityManagerFactory;

    @Before
    public void init() {
        entityManagerFactory = newEntityManagerFactory();
    }

    @After
    public void destroy() {
        entityManagerFactory.close();
    }

    protected abstract Class<?>[] entities();

    private EntityManagerFactory newEntityManagerFactory() {
        Properties properties = getProperties();
        properties.put(org.hibernate.jpa.AvailableSettings.LOADED_CLASSES, Arrays.asList(entities()));
        return Persistence.createEntityManagerFactory("testPersistenceUnit", properties);

    }

    protected Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.ejb.use_class_enhancer", Boolean.TRUE.toString());
        properties.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        //log settings
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
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
        EntityManager entityManager = null;
        EntityTransaction txn = null;
        try {
            entityManager = entityManagerFactory.createEntityManager();
            txn = entityManager.getTransaction();
            txn.begin();
            result = callable.execute(entityManager);
            txn.commit();
        } catch (RuntimeException e) {
            if ( txn != null && txn.isActive() ) txn.rollback();
            throw e;
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
        return result;
    }
}
