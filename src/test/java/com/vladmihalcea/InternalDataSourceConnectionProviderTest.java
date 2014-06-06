package com.vladmihalcea;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.junit.Test;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Properties;

public class InternalDataSourceConnectionProviderTest {

    @Test
    public void test() {
        final SessionFactory sf = buildSessionFactory();
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

    private SessionFactory buildSessionFactory() {
        Properties properties = new Properties();
        properties.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        //log settings
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.show_sql", "true");
        //driver settings
        properties.put("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
        properties.put("hibernate.connection.url", "jdbc:hsqldb:mem:test");
        properties.put("hibernate.connection.username", "sa");
        properties.put("hibernate.connection.password", "");
        //c3p0 settings
        properties.put("hibernate.c3p0.min_size", 1);
        properties.put("hibernate.c3p0.max_size", 5);

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
