package com.vladmihalcea.hibernate.masterclass.laboratory.idgenerator;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.annotations.GenericGenerator;
import org.junit.Test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

import static org.junit.Assert.assertSame;

public class UUIDIdentifierTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
                AssignedUUIDIdentifier.class,
                UUIDIdentifier.class,
                UUID2Identifier.class
        };
    }

    @Test
    public void testAssignedIdentifierGenerator() {
        LOGGER.debug("testAssignedIdentifierGenerator");
        doInTransaction(session -> {
            AssignedUUIDIdentifier assignedUUIDIdentifier = new AssignedUUIDIdentifier();
            LOGGER.debug("persist AssignedUUIDIdentifier");
            session.persist(assignedUUIDIdentifier);
            session.flush();
            assertSame(assignedUUIDIdentifier, session
                    .createQuery("from AssignedUUIDIdentifier where uuid = :uuid")
                    .setParameter("uuid", assignedUUIDIdentifier.uuid)
                    .uniqueResult());
            byte[] uuid = (byte[]) session.createSQLQuery("select uuid from AssignedUUIDIdentifier").uniqueResult();
            LOGGER.debug("merge AssignedUUIDIdentifier");
            session.merge(new AssignedUUIDIdentifier());
        });
    }

    @Test
    public void testUUIDIdentifierGenerator() {
        LOGGER.debug("testUUIDIdentifierGenerator");
        doInTransaction(session -> {
            session.persist(new UUIDIdentifier());
            session.flush();
            session.merge(new UUIDIdentifier());
        });
    }

    @Test
    public void testUUID2IdentifierGenerator() {
        LOGGER.debug("testUUID2IdentifierGenerator");
        doInTransaction(session -> {
            session.persist(new UUID2Identifier());
            session.flush();
            session.merge(new UUID2Identifier());
        });
    }

    @Entity(name = "AssignedUUIDIdentifier")
    public static class AssignedUUIDIdentifier {

        @Id
        @Column(columnDefinition = "BINARY(16)")
        private UUID uuid;

        public AssignedUUIDIdentifier() {
            this.uuid = UUID.randomUUID();
        }
    }

    @Entity(name = "UUIDIdentifier")
    public static class UUIDIdentifier {

        @GeneratedValue(generator = "uuid")
        @GenericGenerator(name = "uuid", strategy = "uuid")
        @Column(columnDefinition = "CHAR(32)")
        @Id
        private String uuidHex;
    }

    @Entity(name = "UUID2Identifier")
    public static class UUID2Identifier {

        @GeneratedValue(generator = "uuid2")
        @GenericGenerator(name = "uuid2", strategy = "uuid2")
        @Column(columnDefinition = "BINARY(16)")
        @Id
        private UUID uuid;
    }
}
