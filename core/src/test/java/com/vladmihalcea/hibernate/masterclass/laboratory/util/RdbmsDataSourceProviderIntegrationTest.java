package com.vladmihalcea.hibernate.masterclass.laboratory.util;

import com.vladmihalcea.book.high_performance_java_persistence.jdbc.batch.AbstractBatchStatementTest;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * RdbmsDataSourceProviderIntegrationTest - Test against some common RDBMS providers
 *
 * @author Vlad Mihalcea
 */
@RunWith(Parameterized.class)
public abstract class RdbmsDataSourceProviderIntegrationTest extends AbstractTest {

    private final RdbmsDataSourceProvider dataSourceProvider;

    public RdbmsDataSourceProviderIntegrationTest(RdbmsDataSourceProvider dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }

    @Parameterized.Parameters
    public static Collection<RdbmsDataSourceProvider[]> rdbmsDataSourceProvider() {
        List<RdbmsDataSourceProvider[]> providers = new ArrayList<>();
        providers.add(new RdbmsDataSourceProvider[] {RdbmsDataSourceProvider.POSTGRESQL});
        providers.add(new RdbmsDataSourceProvider[] {RdbmsDataSourceProvider.ORACLE});
        providers.add(new RdbmsDataSourceProvider[]{RdbmsDataSourceProvider.MYSQL});
        providers.add(new RdbmsDataSourceProvider[]{RdbmsDataSourceProvider.SQLSERVER});
        return providers;
    }

    @Override
    protected RdbmsDataSourceProvider getRdbmsDataSourceProvider() {
        return dataSourceProvider;
    }
}
