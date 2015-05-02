package com.vladmihalcea.hibernate.masterclass.laboratory.mapping;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


/**
 * ElementCollectionTest - Test to check how to speed-up @ElementCollection operations
 *
 * @author Vlad Mihalcea
 */
public class ElementCollectionTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
            Patch.class
        };
    }

    @Before
    public void init() {
        super.init();
        doInTransaction(session -> {
            Patch patch = new Patch();
            patch.getChanges().add(new Change("README.txt", "0a1,5..."));
            patch.getChanges().add(new Change("web.xml", "17c17..."));
            session.persist(patch);
        });
    }

    @Test
    public void testAddingEmbeddable() {
        LOGGER.info("Adding embeddable");
        doInTransaction(session -> {
            Patch patch = (Patch) session.get(Patch.class, 1L);
            patch.getChanges().add(new Change("web.xml", "1d17..."));
        });
    }

    @Test
    public void testAddingFirstEmbeddable() {
        LOGGER.info("Adding first embeddable");
        doInTransaction(session -> {
            Patch patch = (Patch) session.get(Patch.class, 1L);
            patch.getChanges().add(0, new Change("web.xml", "1d17..."));
        });
    }

    @Test
    public void testRemovingEmbeddable() {
        LOGGER.info("Removing embeddable");
        doInTransaction(session -> {
            Patch patch = (Patch) session.get(Patch.class, 1L);
            patch.getChanges().remove(0);
        });
    }

    @Test
    public void testRemovingLastEmbeddable() {
        LOGGER.info("Removing last embeddable");
        doInTransaction(session -> {
            Patch patch = (Patch) session.get(Patch.class, 1L);
            patch.getChanges().remove(patch.getChanges().size() - 1);
        });
    }

    @Test
    public void testRemovingMiddleEmbeddable() {
        LOGGER.info("Removing middle embeddable");
        doInTransaction(session -> {
            Patch patch = (Patch) session.get(Patch.class, 1L);
            patch.getChanges().add(new Change("web.xml", "1d17..."));
            patch.getChanges().add(new Change("server.xml", "3a5..."));
        });
        doInTransaction(session -> {
            Patch patch = (Patch) session.get(Patch.class, 1L);
            patch.getChanges().remove(1);
        });
    }

    /**
     * Patch - Patch
     *
     * @author Vlad Mihalcea
     */
    @Entity(name = "Patch")
    public static class Patch {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        @ElementCollection
        @CollectionTable(
                name="patch_change",
                joinColumns=@JoinColumn(name="patch_id")
        )
        @OrderColumn(name = "index_id")
        private List<Change> changes = new ArrayList<>();

        public List<Change> getChanges() {
            return changes;
        }
    }

    /**
     * Change - Change
     *
     * @author Vlad Mihalcea
     */
    @Embeddable
    public static class Change {

        @Column(name = "path", nullable = false)
        private String path;

        @Column(name = "diff", nullable = false)
        private String diff;

        public Change() {
        }

        public Change(String path, String diff) {
            this.path = path;
            this.diff = diff;
        }

        public String getPath() {
            return path;
        }

        public String getDiff() {
            return diff;
        }
    }
}
