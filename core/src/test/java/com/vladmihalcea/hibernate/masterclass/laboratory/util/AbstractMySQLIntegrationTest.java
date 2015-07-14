package com.vladmihalcea.hibernate.masterclass.laboratory.util;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

/**
 * AbstractMySQLIntegrationTest - Abstract MySQL IntegrationTest
 *
 * @author Vlad Mihalcea
 */
public abstract class AbstractMySQLIntegrationTest extends AbstractTest {

    @Override
    protected String hibernateDialect() {
        return "org.hibernate.dialect.MySQL5Dialect";
    }

    @Override
    protected DataSource dataSource() {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL("jdbc:mysql://localhost/hibernate-master-class?user=mysql&password=admin&rewriteBatchedStatements=true");
        return dataSource;
    }
}
