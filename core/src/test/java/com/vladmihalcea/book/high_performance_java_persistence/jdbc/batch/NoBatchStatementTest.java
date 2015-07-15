package com.vladmihalcea.book.high_performance_java_persistence.jdbc.batch;

import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;

/**
 * BatchStatementTest - Test batching with Statements
 *
 * @author Vlad Mihalcea
 */
public class NoBatchStatementTest extends AbstractBatchStatementTest {

    private int count;

    public NoBatchStatementTest(RdbmsDataSourceProvider dataSourceProvider) {
        super(dataSourceProvider);
    }

    @Override
    protected void onStatement(Statement statement, String dml) throws SQLException {
        statement.executeUpdate(dml);
        count++;
    }

    @Override
    protected void onEnd(Statement statement) throws SQLException {
        assertEquals((getPostCommentCount() + 1) * getPostCount(), count);
    }
}