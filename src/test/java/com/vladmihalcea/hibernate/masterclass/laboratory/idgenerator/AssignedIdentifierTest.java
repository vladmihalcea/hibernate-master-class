package com.vladmihalcea.hibernate.masterclass.laboratory.idgenerator;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.junit.Test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

import static org.junit.Assert.assertSame;

public class AssignedIdentifierTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
                AssignedIdentifier.class,
                UUIDIdentifier.class,
                UUID2Identifier.class
        };
    }

    @Test
    public void testAssignedIdentifierGenerator() {
        LOGGER.debug("testAssignedIdentifierGenerator");
        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(Session session) {
                AssignedIdentifier assignedIdentifier = new AssignedIdentifier(
                        UUID.randomUUID()
                );
                session.persist(assignedIdentifier);
                session.flush();
                assertSame(assignedIdentifier, (AssignedIdentifier) session
                        .createQuery("from assignedIdentifier where uuid = :uuid")
                        .setParameter("uuid", assignedIdentifier.uuid)
                        .uniqueResult());
                byte[] uuid = (byte[]) session.createSQLQuery("select uuid from assignedIdentifier").uniqueResult();

                session.merge(new AssignedIdentifier(
                        UUID.randomUUID()
                ));
                session.flush();
                return null;
            }
        });
    }

    @Test
    public void testUUIDIdentifierGenerator() {
        LOGGER.debug("testUUIDIdentifierGenerator");
        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(Session session) {
                session.persist(new UUIDIdentifier());
                session.flush();
                session.merge(new UUIDIdentifier());
                session.flush();
                return null;
            }
        });
    }

    @Test
    public void testUUID2IdentifierGenerator() {
        LOGGER.debug("testUUID2IdentifierGenerator");
        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(Session session) {
                session.persist(new UUID2Identifier());
                session.flush();
                session.merge(new UUID2Identifier());
                session.flush();
                return null;
            }
        });
    }

    @Entity(name = "assignedIdentifier")
    public static class AssignedIdentifier {

        @Id
        @Column(columnDefinition = "BINARY(16)")
        @Type(type = "org.hibernate.type.UUIDBinaryType")
        private UUID uuid;

        public AssignedIdentifier() {
        }

        public AssignedIdentifier(UUID uuid) {
            this.uuid = uuid;
        }
    }

    @Entity(name = "uuidIdentifier")
    public static class UUIDIdentifier {

        @GeneratedValue(generator = "uuid")
        @GenericGenerator(name = "uuid", strategy = "uuid")
        @Column(columnDefinition = "CHAR(32)")
        @Id
        private String uuidHex;
    }

    @Entity(name = "uuid2Identifier")
    public static class UUID2Identifier {

        public UUID getUuid() {
            return uuid;
        }

        @GeneratedValue(generator = "uuid2")
        @GenericGenerator(name = "uuid2", strategy = "uuid2")
        @Column(columnDefinition = "BINARY(16)")
        @Id
        private UUID uuid;
    }
}
