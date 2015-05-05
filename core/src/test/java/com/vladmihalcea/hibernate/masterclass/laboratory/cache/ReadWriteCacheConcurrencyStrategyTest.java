package com.vladmihalcea.hibernate.masterclass.laboratory.cache;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * NonStrictReadWriteCacheConcurrencyStrategyTest - Test to check CacheConcurrencyStrategy.READ_WRITE
 *
 * @author Vlad Mihalcea
 */
public class ReadWriteCacheConcurrencyStrategyTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
                Repository.class
        };
    }

    @Override
    protected Properties getProperties() {
        Properties properties = super.getProperties();
        properties.put("hibernate.cache.use_second_level_cache", Boolean.TRUE.toString());
        properties.put("hibernate.cache.region.factory_class", "org.hibernate.cache.ehcache.EhCacheRegionFactory");
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
        LOGGER.info("Read-write entities are read-through on persisting");
        doInTransaction(session -> {
            Repository repository = (Repository) session.get(Repository.class, 1L);
            assertNotNull(repository);
        });
        doInTransaction(session -> {
            LOGGER.info("Load Repository from cache");
            session.get(Repository.class, 1L);
        });
    }

    @Test
    public void testRepositoryEntityUpdate() {
        LOGGER.info("Read-write entities are write-through on updating");
        doInTransaction(session -> {
            Repository repository = (Repository) session.get(Repository.class, 1L);
            repository.setName("High-Performance Hibernate");
        });
        doInTransaction(session -> {
            Repository repository = (Repository) session.get(Repository.class, 1L);
            assertEquals("High-Performance Hibernate", repository.getName());
            repository.setName("High-Performance Hibernate Book");
        });
        doInTransaction(session -> {
            Repository repository = (Repository) session.get(Repository.class, 1L);
            assertEquals("High-Performance Hibernate Book", repository.getName());
        });
    }

    /**
     * Repository - Repository
     *
     * @author Vlad Mihalcea
     */
    @Entity(name = "repository")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
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
}
