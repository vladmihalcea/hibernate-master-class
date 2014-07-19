package com.vladmihalcea.hibernate.masterclass.laboratory.idgenerator;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.jdbc.Work;
import org.junit.Test;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class PooledSequenceIdentifierTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
                PooledSequenceIdentifier.class,
        };
    }

    protected Object newEntityInstance() {
        return new PooledSequenceIdentifier();
    }

    @Override
    protected Properties getProperties() {
        Properties properties = super.getProperties();
        properties.put("hibernate.id.new_generator_mappings", "true");
        return properties;
    }

    @Test
    public void testSequenceIdentifierGenerator() {
        LOGGER.debug("testSequenceIdentifierGenerator");
        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(Session session) {
                for (int i = 0; i < 8; i++) {
                    session.persist(newEntityInstance());
                }
                session.flush();
                assertEquals(8, ((Number) session.createSQLQuery("SELECT COUNT(*) FROM sequenceIdentifier").uniqueResult()).intValue());
                insertNewRow(session);
                insertNewRow(session);
                insertNewRow(session);
                assertEquals(11, ((Number) session.createSQLQuery("SELECT COUNT(*) FROM sequenceIdentifier").uniqueResult()).intValue());
                List<Number> ids = session.createSQLQuery("SELECT id FROM sequenceIdentifier").list();
                for(Number id : ids) {
                    LOGGER.debug("Found id: {}", id);
                }
                for (int i = 0; i < 3; i++) {
                    session.persist(newEntityInstance());
                }
                session.flush();
                return null;
            }
        });
    }

    @Entity(name = "sequenceIdentifier")
    public static class PooledSequenceIdentifier {

        @Id
        @GenericGenerator(name = "sampleGenerator", strategy = "enhanced-sequence",
                parameters = {
                        @org.hibernate.annotations.Parameter(name = "optimizer",
                                value = "pooled"
                        ),
                        @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                        @org.hibernate.annotations.Parameter(name = "increment_size", value = "5")
                }
        )
        @GeneratedValue(strategy = GenerationType.TABLE, generator = "sampleGenerator")
        private Long id;
    }

    private void insertNewRow(Session session) {
        session.doWork(new Work() {
            @Override
            public void execute(Connection connection) throws SQLException {
                Statement statement = null;
                try {
                    statement = connection.createStatement();
                    statement.executeUpdate("INSERT INTO sequenceIdentifier VALUES NEXT VALUE FOR hibernate_sequence");
                } finally {
                    if (statement != null) {
                        statement.close();
                    }
                }
            }
        });
    }

    private int getNextSequenceValue(Session session) {
        final AtomicInteger returnValue = new AtomicInteger();
        session.doWork(new Work() {
            @Override
            public void execute(Connection connection) throws SQLException {
                Statement sequenceCaller = null;
                try {
                    sequenceCaller = connection.createStatement();
                    sequenceCaller.execute("CALL NEXT VALUE FOR hibernate_sequence");
                    ResultSet resultSet = sequenceCaller.getResultSet();
                    resultSet.next();
                    returnValue.set(resultSet.getInt(1));
                } finally {
                    if (sequenceCaller != null) {
                        sequenceCaller.close();
                    }
                }
            }
        });
        return returnValue.get();
    }
}
