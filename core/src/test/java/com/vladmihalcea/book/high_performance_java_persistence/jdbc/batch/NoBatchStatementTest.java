package com.vladmihalcea.book.high_performance_java_persistence.jdbc.batch;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractPostgreSQLIntegrationTest;
import org.junit.Test;

import javax.persistence.*;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * BatchStatementTest - Test batching with Statements
 *
 * @author Vlad Mihalcea
 */
public class NoBatchStatementTest extends AbstractBatchStatementTest {

    private int count;

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