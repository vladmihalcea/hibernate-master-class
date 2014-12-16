package com.vladmihalcea.hibernate.masterclass.laboratory.testenv;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import java.util.Properties;

public class InternalDataSourceConnectionProviderTest extends AbstractConnectionProviderTest {

    protected Properties getProperties() {
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
        return properties;
    }

    protected SessionFactory newSessionFactory() {
        Properties properties = getProperties();

        return new Configuration()
                .addProperties(properties)
                .addAnnotatedClass(SecurityId.class)
                .buildSessionFactory(
                        new StandardServiceRegistryBuilder()
                                .applySettings(properties)
                                .build()
        );
    }
}
