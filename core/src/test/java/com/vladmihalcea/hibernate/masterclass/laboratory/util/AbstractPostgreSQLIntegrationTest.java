package com.vladmihalcea.hibernate.masterclass.laboratory.util;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

/**
 * AbstractPostgreSQLIntegrationTest - Abstract PostgreSQL IntegrationTest
 *
 * @author Vlad Mihalcea
 */
public abstract class AbstractPostgreSQLIntegrationTest extends AbstractTest {

    @Override
    protected String hibernateDialect() {
        return "org.hibernate.dialect.PostgreSQLDialect";
    }

    @Override
    protected DataSource dataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setDatabaseName("hibernate-master-class");
        dataSource.setServerName("localhost");
        dataSource.setUser("postgres");
        dataSource.setPassword("admin");
        return dataSource;
    }
}
