package com.vladmihalcea.hibernate.masterclass.laboratory.cache;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractPostgreSQLIntegrationTest;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Interceptor;
import org.hibernate.Transaction;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.cache.spi.CacheKey;
import org.hibernate.cache.spi.EntityRegion;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


/**
 * ReadWriteCacheConcurrencyStrategyWithTimeoutTest - Test to check CacheConcurrencyStrategy.READ_WRITE with lock timeout
 *
 * @author Vlad Mihalcea
 */
public class ReadWriteCacheConcurrencyStrategyWithLockTimeoutTest extends AbstractPostgreSQLIntegrationTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
                Repository.class
        };
    }

    private AtomicBoolean applyInterceptor = new AtomicBoolean();

    @Override
    protected Interceptor interceptor() {
        return new EmptyInterceptor() {
            @Override
            public void beforeTransactionCompletion(Transaction tx) {
                if(applyInterceptor.get()) {
                    tx.rollback();
                }
            }
        };
    }

    @Override
    protected Properties getProperties() {
        Properties properties = super.getProperties();
        properties.put("hibernate.cache.use_second_level_cache", Boolean.TRUE.toString());
        properties.put("hibernate.cache.region.factory_class", "org.hibernate.cache.ehcache.EhCacheRegionFactory");
        properties.put("net.sf.ehcache.hibernate.cache_lock_timeout", String.valueOf(250));
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
        long timeout = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        LOGGER.info("Read-write entities are write-through on updating");
        try {
            doInTransaction(session -> {
                Repository repository = (Repository) session.get(Repository.class, 1L);
                repository.setName("High-Performance Hibernate");
                applyInterceptor.set(true);
            });
        } catch (Exception e) {
            LOGGER.info("Expected", e);
        }
        applyInterceptor.set(false);

        AtomicReference<Object> entryReference = new AtomicReference<>();
        AtomicBoolean hasChanged = new AtomicBoolean();

        while (entryReference.get() == null || !hasChanged.get()) {
            doInTransaction(session -> {
                Repository repository = (Repository) session.get(Repository.class, 1L);
                EntityPersister entityPersister = ((SessionFactoryImplementor) getSessionFactory()).getEntityPersister(Repository.class.getName() );
                EntityRegion region = entityPersister.getCacheAccessStrategy().getRegion();
                Field cacheField = getField(region.getClass(), "cache");
                try {
                    net.sf.ehcache.Cache cache = (net.sf.ehcache.Cache) cacheField.get(region);
                    Object entry = entryReference.get();
                    Object _entry = cache.get(cacheKey(1L, entityPersister));
                    if(entry != null) {
                        hasChanged.set(entry != _entry);
                    }
                    entryReference.set(_entry);
                    LOGGER.info("Cache entry {}", ToStringBuilder.reflectionToString(_entry));
                } catch (Exception e) {
                    LOGGER.error("Error accessing Cache", e);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    private Field getField(Class clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            if(clazz.getSuperclass() != null && clazz.getSuperclass() != clazz) {
                return getField(clazz.getSuperclass(), fieldName);
            }
        }
        throw new IllegalArgumentException("No field " + fieldName + " found!");
    }

    private CacheKey cacheKey(Serializable identifier, EntityPersister p) {
        return new CacheKey(
                identifier,
                p.getIdentifierType(),
                p.getRootEntityName(),
                null,
                (SessionFactoryImplementor) getSessionFactory()
        );
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
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;

        @Version
        private int version;

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
