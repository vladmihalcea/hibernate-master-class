package com.vladmihalcea.hibernate.masterclass.laboratory.cache;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;


/**
 * ReadOnlyCacheConcurrencyStrategyTest - Test to check CacheConcurrencyStrategy.READ_ONLY
 *
 * @author Vlad Mihalcea
 */
public class ReadOnlyCacheConcurrencyStrategyTest extends AbstractTest {

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
        properties.put("hibernate.cache.use_structured_entries", Boolean.TRUE.toString());
        
        return properties;
    }

    @Before
    public void init() {
        super.init();
        doInTransaction(session -> {
            Repository repository = new Repository("Hibernate-Master-Class");
            session.persist(repository);
        });
    }

    @Test
    public void testRepositoryEntityLoad() {

        LOGGER.info("Read-only entities are read-through");

        doInTransaction(session -> {
            Repository repository = (Repository) session.get(Repository.class, 1L);
            assertNotNull(repository);
        });

        printEntityCacheStats(Repository.class.getName(), true);

        doInTransaction(session -> {
            LOGGER.info("Load Repository from cache");
            session.get(Repository.class, 1L);
        });
        
        printEntityCacheStats(Repository.class.getName(), true);

    }

    @Test
    public void testCollectionCache() {
        LOGGER.info("Collections require separate caching");
        doInTransaction(session -> {
            Repository repository = (Repository)
                    session.get(Repository.class, 1L);
            Commit commit = new Commit(repository);
            commit.getChanges().add(
                    new Change("README.txt", "0a1,5...")
            );
            commit.getChanges().add(
                    new Change("web.xml", "17c17...")
            );
            session.persist(commit);
        });
        
        printEntityCacheStats(Repository.class.getName());
        printEntityCacheStats(Commit.class.getName());

        doInTransaction(session -> {
            LOGGER.info("Load Commit from database ");
            Commit commit = (Commit)
                    session.get(Commit.class, 1L);
            assertEquals(2, commit.getChanges().size());
        });

        printEntityCacheStats(Repository.class.getName());
        printEntityCacheStats(Commit.class.getName());

        doInTransaction(session -> {
            LOGGER.info("Load Commit from cache");
            Commit commit = (Commit)
                    session.get(Commit.class, 1L);
            assertEquals(2, commit.getChanges().size());
        });

        printEntityCacheStats(Repository.class.getName());
        printEntityCacheStats(Commit.class.getName());
        
    }

    @Test
    public void testReadOnlyEntityUpdate() {
        try {
            LOGGER.info("Read-only cache entries cannot be updated");
            doInTransaction(session -> {
                Repository repository = (Repository) session.get(Repository.class, 1L);

                printEntityCacheStats(Repository.class.getName());
                
                repository.setName("High-Performance Hibernate");
            });
        } catch (Exception e) {
            LOGGER.error("Expected", e);
        }
    }

    @Test
    public void testReadOnlyEntityDelete() {
        LOGGER.info("Read-only cache entries can be deleted");
        doInTransaction(session -> {
            Repository repository = (Repository) session.get(Repository.class, 1L);
            assertNotNull(repository);
            session.delete(repository);
        });
        
        printEntityCacheStats(Repository.class.getName());
        
        doInTransaction(session -> {
            Repository repository = (Repository) session.get(Repository.class, 1L);
            printEntityCacheStats(Repository.class.getName());
            assertNull(repository);
        });
    }


    /**
     * Repository - Repository
     *
     * @author Vlad Mihalcea
     */
    @Entity(name = "repository")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
    public static class Repository {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private String name;

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
    }

    /**
     * Commit - Commit
     *
     * @author Vlad Mihalcea
     */
    @Entity(name = "Commit")
    @Table(name = "commit")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
    @Immutable
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
        private List<Change> changes = new ArrayList<>();

        public Commit() {
        }

        public Commit(Repository repository) {
            this.repository = repository;
        }

        public Repository getRepository() {
            return repository;
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
