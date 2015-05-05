package com.vladmihalcea.hibernate.masterclass.laboratory.cache;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * ReadWriteCacheConcurrencyStrategyWithConcurrentUpdateTest - Test to check CacheConcurrencyStrategy.READ_WRITE
 *
 * @author Vlad Mihalcea
 */
public class ReadWriteCacheConcurrencyStrategyWithConcurrentUpdateTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
                Repository.class
        };
    }

    private AtomicBoolean ready = new AtomicBoolean();
    private final CountDownLatch endLatch = new CountDownLatch(1);

    @Override
    protected Interceptor interceptor() {
        return new EmptyInterceptor() {
            @Override
            public void beforeTransactionCompletion(Transaction tx) {
                if(ready.get()) {
                    LOGGER.info("Overwrite Repository concurrently");
                    executeAsync(() -> {
                        Session _session = getSessionFactory().openSession();
                        Repository repository = (Repository) _session.get(Repository.class, 1L);
                        repository.setName("High-Performance Hibernate Book");
                        _session.close();
                        endLatch.countDown();
                    });
                }
            }
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
    public void testRepositoryEntityUpdate() throws InterruptedException {
        LOGGER.info("Read-write entities are write-through on updating");
        doInTransaction(session -> {
            Repository repository = (Repository) session.get(Repository.class, 1L);
            repository.setName("High-Performance Hibernate");
            ready.set(true);
        });
        endLatch.await();
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
