package com.vladmihalcea.hibernate.masterclass.laboratory.batch;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractIntegrationTest;
import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.dialect.Dialect;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * BatchSizeTest - Test to check hibernate.jdbc.batch_size
 *
 * @author Vlad Mihalcea
 */
public class BatchSizeTest extends AbstractIntegrationTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
            Post.class,
        };
    }

    @Override
    protected Properties getProperties() {
        Properties properties = super.getProperties();
        properties.put("hibernate.jdbc.batch_versioned_data", "true");
        return properties;
    }

    @Test
    public void testDefaultBatchSize() throws InterruptedException {
        LOGGER.info("Test default batch size");
        long startNanos = System.nanoTime();
        doInTransaction(session -> {
            int batchSize = batchSize();
            for(int i = 0; i < itemsCount(); i++) {
                session.persist(new Post(String.format("Post no. %d", i)));
                if(i % batchSize == 0 && i > 0) {
                    session.flush();
                    session.clear();
                }
            }
        });
        LOGGER.info("{} took {} millis", getClass().getSimpleName(), TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos));
    }

    protected int itemsCount() {
        return 100;
    }

    protected int batchSize() {
        return Integer.valueOf(Dialect.DEFAULT_BATCH_SIZE);
    }

    @Entity(name = "Post")
    public static class Post {

        @Id
        @GenericGenerator(name = "sequenceGenerator", strategy = "enhanced-sequence",
            parameters = {
                @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled-lo"),
                @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                @org.hibernate.annotations.Parameter(name = "increment_size", value = "30")
            }
        )
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
        private Long id;

        private String title;

        public Post() {
        }

        public Post(String title) {
            this.title = title;
        }
    }
}
