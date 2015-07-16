package com.vladmihalcea.hibernate.masterclass.laboratory.util;

/**
 * AbstractMySQLIntegrationTest - Abstract MySQL IntegrationTest
 *
 * @author Vlad Mihalcea
 */
public abstract class AbstractMySQLIntegrationTest extends AbstractTest {

    @Override
    protected DataSourceProvider getDataSourceProvider() {
        return new MySQLDataSourceProvider();
    }
}
