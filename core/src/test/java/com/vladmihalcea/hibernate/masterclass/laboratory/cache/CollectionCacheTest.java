package com.vladmihalcea.hibernate.masterclass.laboratory.cache;

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
 * CollectionCacheTest - Test to check Collection Cache
 *
 * @author Vlad Mihalcea
 */
public class CollectionCacheTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
                Repository.class,
                Commit.class
        };
    }

    @Override
    protected Properties getProperties() {
        Properties properties = super.getProperties();
        properties.put("hibernate.cache.use_second_level_cache", Boolean.TRUE.toString());
        properties.put("hibernate.cache.region.factory_class", "org.hibernate.cache.ehcache.EhCacheRegionFactory");
        properties.put("hibernate.cache.default_cache_concurrency_strategy", CacheConcurrencyStrategy.READ_WRITE.name());
        return properties;
    }

    @Before
    public void init() {
        super.init();
        doInTransaction(session -> {
            Repository repository = new Repository("Hibernate-Master-Class");
            session.persist(repository);

            Commit commit1 = new Commit();
            commit1.getChanges().add(new Change("README.txt", "0a1,5..."));
            commit1.getChanges().add(new Change("web.xml", "17c17..."));

            Commit commit2 = new Commit();
            commit2.getChanges().add(new Change("README.txt", "0b2,5..."));

            repository.addCommit(commit1);
            repository.addCommit(commit2);
            session.persist(commit1);
        });
        doInTransaction(session -> {
            Repository repository = (Repository) session.get(Repository.class, 1L);
            for (Commit commit : repository.getCommits()) {
                assertFalse(commit.getChanges().isEmpty());
            }
        });
    }

    @Test
    public void testInvalidateEntityCollectionCacheOnRemovingEntries() {
        LOGGER.info("Invalidate entity collection cache on removing entries");
        doInTransaction(session -> {
            Repository repository = (Repository) session.get(Repository.class, 1L);
            assertEquals(2, repository.getCommits().size());
            repository.removeCommit(repository.getCommits().get(0));
        });
        doInTransaction(session -> {
            Repository repository = (Repository) session.get(Repository.class, 1L);
            assertEquals(1, repository.getCommits().size());
        });
    }

    @Test
    public void testInvalidateEntityCollectionCacheOnAddingEntries() {
        LOGGER.info("Invalidate entity collection cache on adding entries");
        doInTransaction(session -> {
            Repository repository = (Repository) session.get(Repository.class, 1L);
            assertEquals(2, repository.getCommits().size());

            Commit commit = new Commit();
            commit.getChanges().add(new Change("Main.java", "0b3,17..."));
            repository.addCommit(commit);
        });
        doInTransaction(session -> {
            Repository repository = (Repository) session.get(Repository.class, 1L);
            assertEquals(3, repository.getCommits().size());
        });
    }

    @Test
    public void testConsistencyIssuesWhenRemovingChildDirectly() {
        LOGGER.info("Removing a Child directly may cause inconsistencies");
        doInTransaction(session -> {
            Commit commit = (Commit) session.get(Commit.class, 1L);
            session.delete(commit);
        });
        try {
            doInTransaction(session -> {
                Repository repository = (Repository) session.get(Repository.class, 1L);
                assertEquals(1, repository.getCommits().size());
            });
        } catch (ObjectNotFoundException e) {
            LOGGER.warn("Object not found", e);
        }
    }

    @Test
    public void testInvalidateEmbeddableCollectionCacheOnRemovingEntries() {
        LOGGER.info("Invalidate embeddable collection cache on removing entries");
        doInTransaction(session -> {
            Commit commit = (Commit) session.get(Commit.class, 1L);
            assertEquals(2, commit.getChanges().size());
            commit.getChanges().remove(0);
        });
        doInTransaction(session -> {
            Commit commit = (Commit) session.get(Commit.class, 1L);
            assertEquals(1, commit.getChanges().size());
        });
    }

    @Test
    public void testInvalidateEmbeddableCollectionCacheOnAddingEntries() {
        LOGGER.info("Invalidate embeddable collection cache on adding entries");
        doInTransaction(session -> {
            Commit commit = (Commit) session.get(Commit.class, 1L);
            assertEquals(2, commit.getChanges().size());
            commit.getChanges().add(new Change("Main.java", "0b3,17..."));
        });
        doInTransaction(session -> {
            Commit commit = (Commit) session.get(Commit.class, 1L);
            assertEquals(3, commit.getChanges().size());
        });
    }

    /**
     * Repository - Repository
     *
     * @author Vlad Mihalcea
     */
    @Entity(name = "Repository")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    public static class Repository {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private String name;

        @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
        @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Commit> commits = new ArrayList<>();

        public Repository() {
        }

        public Repository(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public List<Commit> getCommits() {
            return commits;
        }

        public void addCommit(Commit commit) {
            commits.add(commit);
            commit.setRepository(this);
        }

        public void removeCommit(Commit commit) {
            commits.remove(commit);
            commit.setRepository(null);
        }
    }

    /**
     * Commit - Commit
     *
     * @author Vlad Mihalcea
     */
    @Entity(name = "Commit")
    @Table(name = "commit")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    public static class Commit {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        private Repository repository;

        @ElementCollection
        @CollectionTable(
                name="commit_change",
                joinColumns=@JoinColumn(name="commit_id")
        )
        @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
        //@CollectionId(columns = @Column(name = "id"), type = @Type(type = "long"), generator = "sequence")
        private List<Change> changes = new ArrayList<>();

        public Commit() {
        }

        public Repository getRepository() {
            return repository;
        }

        public void setRepository(Repository repository) {
            this.repository = repository;
        }

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

        private String path;

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
