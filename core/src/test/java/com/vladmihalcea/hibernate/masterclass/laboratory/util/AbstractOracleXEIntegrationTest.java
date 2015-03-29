package com.vladmihalcea.hibernate.masterclass.laboratory.util;

import oracle.jdbc.pool.OracleDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * AbstractOracleXEIntegrationTest - Abstract Orcale XE IntegrationTest
 *
 * @author Vlad Mihalcea
 */
public abstract class AbstractOracleXEIntegrationTest extends AbstractTest {

    @Override
    protected String hibernateDialect() {
        return "org.hibernate.dialect.Oracle10gDialect";
    }

    @Override
    protected DataSource dataSource() {
        try {
            OracleDataSource dataSource = new OracleDataSource();
            dataSource.setDatabaseName("hibernate-master-class");
            dataSource.setURL("jdbc:oracle:thin:@localhost:1521/xe");
            dataSource.setUser("sys as sysdba");
            dataSource.setPassword("admin");
            return dataSource;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
