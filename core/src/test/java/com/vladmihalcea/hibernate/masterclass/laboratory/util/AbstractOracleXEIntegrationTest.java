package com.vladmihalcea.hibernate.masterclass.laboratory.util;

/**
 * AbstractOracleXEIntegrationTest - Abstract Orcale XE IntegrationTest
 *
 * @author Vlad Mihalcea
 */
public abstract class AbstractOracleXEIntegrationTest extends AbstractTest {

    @Override
    protected DataSourceProvider getDataSourceProvider() {
        return new OracleDataSourceProvider();
    }
}
