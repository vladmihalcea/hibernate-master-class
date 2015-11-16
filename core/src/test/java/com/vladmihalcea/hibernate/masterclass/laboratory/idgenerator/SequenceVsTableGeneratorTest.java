package com.vladmihalcea.hibernate.masterclass.laboratory.idgenerator;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.Session;
import org.junit.Test;

import javax.persistence.*;
import java.util.Properties;

public class SequenceVsTableGeneratorTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
                SequenceIdentifier.class,
                TableSequenceIdentifier.class
        };
    }

    @Override
    protected Properties getProperties() {
        Properties properties = super.getProperties();
        return properties;
    }

    @Test
    public void testSequenceIdentifierGenerator() {
        LOGGER.debug("testSequenceIdentifierGenerator");
        doInTransaction(session -> {
            for (int i = 0; i < 5; i++) {
                session.persist(new SequenceIdentifier());
            }
            session.flush();
        });
    }

    @Test
    public void testTableSequenceIdentifierGenerator() {
        LOGGER.debug("testTableSequenceIdentifierGenerator");
        doInTransaction(session -> {
            for (int i = 0; i < 5; i++) {
                session.persist(new TableSequenceIdentifier());
            }
            session.flush();
        });
    }

    @Entity(name = "sequenceIdentifier")
    public static class SequenceIdentifier {

        @Id
        @GeneratedValue(generator = "sequence", strategy=GenerationType.SEQUENCE)
        @SequenceGenerator(name = "sequence", allocationSize = 10)
        private Long id;
    }

    @Entity(name = "tableIdentifier")
    public static class TableSequenceIdentifier {

        @Id
        @GeneratedValue(generator = "table", strategy=GenerationType.TABLE)
        @TableGenerator(name = "table", allocationSize = 10)
        private Long id;
    }
}
