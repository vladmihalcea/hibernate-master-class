package com.vladmihalcea.book.high_performance_java_persistence.jdbc.batch.generatedkeys.sequence;

import com.vladmihalcea.book.high_performance_java_persistence.jdbc.batch.providers.SequenceBatchEntityProvider;
import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AbstractSequenceGeneratedKeysBatchPreparedStatementTest - Base class for testing JDBC PreparedStatement generated keys for Sequences
 *
 * @author Vlad Mihalcea
 */
@RunWith(Parameterized.class)
public abstract class AbstractSequenceGeneratedKeysBatchPreparedStatementTest extends AbstractTest {

    private SequenceBatchEntityProvider entityProvider = new SequenceBatchEntityProvider();

    private int allocationSize = 1;

    public AbstractSequenceGeneratedKeysBatchPreparedStatementTest(int allocationSize) {
        this.allocationSize = allocationSize;
    }

    @Parameterized.Parameters
    public static Collection<Integer[]> rdbmsDataSourceProvider() {
        List<Integer[]> providers = new ArrayList<>();
        providers.add(new Integer[]{1});
        providers.add(new Integer[]{5});
        providers.add(new Integer[]{10});
        providers.add(new Integer[]{15});
        providers.add(new Integer[]{20});
        providers.add(new Integer[]{25});
        return providers;
    }

    @Override
    protected Class<?>[] entities() {
        return entityProvider.entities();
    }

    @Test
    public void testBatch() {
        doInConnection(this::batchInsert);
    }

    protected int getPostCount() {
        return 5 * 1000;
    }

    protected int getBatchSize() {
        return 25;
    }

    protected int getAllocationSize() {
        return allocationSize;
    }

    protected void batchInsert(Connection connection) throws SQLException {
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        LOGGER.info("{} Driver supportsGetGeneratedKeys: {}", getDataSourceProvider().database(), databaseMetaData.supportsGetGeneratedKeys());

        dropSequence(connection);
        createSequence(connection);

        int count = 0;

        long startNanos = System.nanoTime();

        try(PreparedStatement postStatement = connection.prepareStatement(
                "insert into Post (id, title, version) " +
                "values (?, ?, ?)")) {
            int postCount = getPostCount();

            for (int i = 0; i < postCount; i++) {
                int index = 0;

                long id;

                try(Statement statement = connection.createStatement()) {
                    try(ResultSet resultSet = statement.executeQuery(
                            callSequenceSyntax())) {
                        resultSet.next();
                        id = resultSet.getLong(1);
                    }
                }

                postStatement.setLong(++index, id);
                postStatement.setString(++index, String.format("Post no. %1$d", i));
                postStatement.setInt(++index, 0);
                postStatement.addBatch();
                if(++count % getBatchSize() == 0) {
                    postStatement.executeBatch();
                }
            }
            postStatement.executeBatch();
        }

        LOGGER.info("{}.testInsert for {} using allocation size {} took {} millis",
                getClass().getSimpleName(),
                getDataSourceProvider().getClass().getSimpleName(),
                getAllocationSize(),
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos));
    }

    protected abstract String callSequenceSyntax();

    protected void dropSequence(Connection connection) {
        try(Statement statement = connection.createStatement()) {
            statement.executeUpdate("drop sequence post_seq");
        } catch (Exception ignore) {}
    }

    protected void createSequence(Connection connection) {
        try(Statement statement = connection.createStatement()) {
            statement.executeUpdate(String.format("create sequence post_seq start with 1 increment by %d", getAllocationSize()));
        } catch (Exception ignore) {}
    }
}
