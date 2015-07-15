package com.vladmihalcea.hibernate.masterclass.laboratory.util;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

/**
 * AbstractSQLServerIntegrationTest - Abstract SQL Server IntegrationTest
 *
 * @author Vlad Mihalcea
 */
public abstract class AbstractSQLServerIntegrationTest extends AbstractTest {

    @Override
    protected String hibernateDialect() {
        return "org.hibernate.dialect.SQLServer2012Dialect";
    }

    @Override
    protected DataSource dataSource() {
        SQLServerDataSource dataSource = new SQLServerDataSource();
        //dataSource.setURL("jdbc:sqlserver://localhost;instance=SQLEXPRESS;databaseName=hibernate_master_class;integratedSecurity=true");
        //dataSource.setURL("jdbc:sqlserver://localhost\\sqlexpress;databaseName=hibernate_master_class;integratedSecurity=true");
        dataSource.setURL("jdbc:sqlserver://localhost;instance=SQLEXPRESS;databaseName=hibernate_master_class;user=sa;password=adm1n");
        return dataSource;
    }
}
