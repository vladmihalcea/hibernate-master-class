package com.vladmihalcea.hibernate.masterclass.laboratory.inheritance;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;
import org.junit.Ignore;
import org.junit.Test;

import javax.persistence.*;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.UUID;

import static org.junit.Assert.fail;

public class OverrideIdentifierDefinitionTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
                Book.class,
                Order.class,
                WebResource.class,
                InfoId.class,
                InfoLine.class
        };
    }

    @Test
    public void testAddWebResource() {
        LOGGER.debug("testAddWebResource");
        doInTransaction(session -> {
                try {
                    WebResource webResource = new WebResource();
                    webResource.setId(new URL("http://vladmihalcea.com"));
                    session.persist(webResource);
                    session.flush();
                } catch (MalformedURLException e) {
                    fail(e.getMessage());
                }
                return null;

        });
    }

    @Test
    @Ignore
    public void testAddOrder() {
        LOGGER.debug("testAddOrder");
        doInTransaction(session -> {
                Order order = new Order();
                session.persist(order);
                session.flush();
                return null;

        });
    }

    @Test
    public void testAddBook() {
        LOGGER.debug("testAddBook");
        doInTransaction(session -> {
                Book book = new Book();
                book.setId(1234567890123L);
                session.persist(book);
                session.flush();
                return null;

        });
    }

    @Test
    @Ignore("not finished yet")
    public void testAddInfoId() {
        LOGGER.debug("testAddInfoId");
        doInTransaction(session -> {
                InfoId infoId = new InfoId();
                //infoId.setId(UUID.randomUUID());
                session.persist(infoId);
                session.flush();
                return null;

        });
    }

    @Test
    @Ignore("not finished yet")
    public void testAddInfoLine() {
        LOGGER.debug("testAddInfoLine");
        doInTransaction(session -> {
                InfoLine infoLine = new InfoLine();
                //infoId.setId(UUID.randomUUID());
                session.persist(infoLine);
                session.flush();
                return null;

        });
    }

    /**
     * WebResource - Web Resource
     *
     * @author Vlad Mihalcea
     */
    @Entity(name = "web_resource")
    public static class WebResource extends AssignableIdentifierEntity<URL>{
    }

    /**
     * Order - Order
     *
     * @author Vlad Mihalcea
     */
    @Entity(name = "order_line")
    @AttributeOverride(name = "id", column = @Column(columnDefinition = "BINARY(16)"))
    public static class Order extends AssignableIdentifierEntity<UUID> {

        @Override
        @GeneratedValue(generator = "uuid2")
        @GenericGenerator(name = "uuid2", strategy = "uuid2")
        public UUID getId() {
            return super.getId();
        }
    }

    @Entity(name = "info_id")
    @AttributeOverride(name = "id", column = @Column(columnDefinition = "BINARY(16)"))
    public static class InfoId extends GenericGeneratorIdentifierEntity<UUID> {

    }

    @Entity(name = "info_line")
    @GenericGenerator(name = "extendable-generator", strategy = "uuid2")
    @AttributeOverride(name = "id", column = @Column(columnDefinition = "BINARY(16)"))
    public static class InfoLine extends GenericGeneratorIdentifierEntity<UUID> {

    }

    /**
     * Book - Book
     *
     * Id - <a href="http://en.wikipedia.org/wiki/International_Standard_Book_Number">ISBN</>
     *
     * @author Vlad Mihalcea
     */
    @Entity(name = "book")
    @AttributeOverride(name = "id", column = @Column(name = "isbn", length = 13))
    public static class Book extends AssignableIdentifierEntity<Long> {

        private String title;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    /**
     * AssignableIdentifierEntity - Entity with an assigned identifier
     *
     * @author Vlad Mihalcea
     */
    @MappedSuperclass
    public abstract static class AssignableIdentifierEntity<ID extends Serializable> extends BaseEntity<ID> {

        private ID id;

        @Override
        @Id
        @Access(AccessType.PROPERTY)
        public ID getId() {
            return id;
        }

        @Override
        public void setId(ID id) {
            this.id = id;
        }
    }

    @MappedSuperclass
    public abstract static class GenericGeneratorIdentifierEntity<ID extends Serializable> extends BaseEntity<ID> {

        @Id
        @GeneratedValue(generator = "extendable-generator")
        @GenericGenerator(name = "extendable-generator", strategy = "assigned")
        private ID id;

        @Override
        public ID getId() {
            return id;
        }

        @Override
        public void setId(ID id) {
            this.id = id;
        }
    }

    /**
     * BaseEntity - Base Entity
     *
     * @author Vlad Mihalcea
     */
    @MappedSuperclass
    @Access(AccessType.FIELD)
    public abstract static class BaseEntity<ID extends Serializable> implements Serializable {

        @Column(name = "creation_time", updatable=false)
        private Timestamp creationTime;

        @Column(name = "modification_time")
        private Timestamp modificationTime;

        @Version
        private int version;

        public abstract ID getId();
        public abstract void setId(ID id);

        public final int getVersion() {
            return version;
        }

        @PreUpdate
        public final void preUpdate() {
            modificationTime = new Timestamp(System.currentTimeMillis());
        }

        @PrePersist
        public final void prePersist() {
            creationTime = new Timestamp(System.currentTimeMillis());
            modificationTime = new Timestamp(System.currentTimeMillis());
        }

        public Timestamp getCreationTime() {
            return creationTime;
        }

        public Timestamp getModificationTime() {
            return modificationTime;
        }

    }
}
