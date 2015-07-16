package com.vladmihalcea.book.high_performance_java_persistence.jdbc.batch;

import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;

/**
 * BatchStatementTest - Test batching with Statements
 *
 * @author Vlad Mihalcea
 */
public class BatchStatementTest extends AbstractBatchStatementTest {

    public BatchStatementTest(DataSourceProvider dataSourceProvider) {
        super(dataSourceProvider);
    }

    @Override
    protected void onStatement(Statement statement, String dml) throws SQLException {
       statement.addBatch(dml);
    }

    @Override
    protected void onEnd(Statement statement) throws SQLException {
        int[] updateCount = statement.executeBatch();
        statement.clearBatch();
        assertEquals((getPostCommentCount() + 1) * getPostCount(), updateCount.length);
    }

    @Override
    protected void onFlush(Statement statement) throws SQLException {
        statement.executeBatch();
    }
}
