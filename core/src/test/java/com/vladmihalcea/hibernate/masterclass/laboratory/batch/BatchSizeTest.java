package com.vladmihalcea.hibernate.masterclass.laboratory.batch;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractIntegrationTest;
import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    @Test
    public void testCascadeLockOnManagedEntity() throws InterruptedException {
        LOGGER.info("Test lock cascade for managed entity");
        doInTransaction(session -> {
            Post post = (Post) session.createQuery(
                "select p " +
                "from Post p " +
                "join fetch p.details " +
                "where " +
                "   p.id = :id"
            ).setParameter("id", 1L)
            .uniqueResult();
            session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_WRITE)).setScope(true).lock(post);
        });
    }

    @Entity(name = "Post")
    public static class Post {

        @Id
        @GenericGenerator(name = "sequenceGenerator", strategy = "enhanced-sequence",
            parameters = {
                @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled-lo"),
                @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                @org.hibernate.annotations.Parameter(name = "increment_size", value = "100")
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
