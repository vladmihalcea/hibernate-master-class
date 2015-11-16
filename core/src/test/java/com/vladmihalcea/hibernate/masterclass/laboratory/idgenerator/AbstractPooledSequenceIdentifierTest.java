package com.vladmihalcea.hibernate.masterclass.laboratory.idgenerator;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public abstract class AbstractPooledSequenceIdentifierTest extends AbstractTest {

    protected abstract Object newEntityInstance();

    @Override
    protected Properties getProperties() {
        Properties properties = super.getProperties();
        properties.put("hibernate.id.new_generator_mappings", "true");
        return properties;
    }

    protected void insertSequences() {
        LOGGER.debug("testSequenceIdentifierGenerator");
        doInTransaction(session -> {
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
            for (Number id : ids) {
                LOGGER.debug("Found id: {}", id);
            }
            for (int i = 0; i < 3; i++) {
                session.persist(newEntityInstance());
            }
            session.flush();
        });
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
}
