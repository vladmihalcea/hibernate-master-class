package com.vladmihalcea.hibernate.masterclass.laboratory.cache;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractPostgreSQLIntegrationTest;
import org.hibernate.Session;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * SecondLevelCacheTest - Test to check the 2nd level cache
 *
 * @author Vlad Mihalcea
 */
public class QueryCacheTest extends AbstractPostgreSQLIntegrationTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
                Post.class,
        };
    }

    @Override
    protected Properties getProperties() {
        Properties properties = super.getProperties();
        properties.put("hibernate.cache.use_second_level_cache", Boolean.TRUE.toString());
        properties.put("hibernate.cache.use_query_cache", Boolean.TRUE.toString());
        properties.put("hibernate.cache.region.factory_class", "org.hibernate.cache.ehcache.EhCacheRegionFactory");
        return properties;
    }

    @Before
    public void init() {
        super.init();
        doInTransaction(session -> {
            Post post = new Post();
            post.setName("Hibernate Master Class");
            session.persist(post);
        });
    }

    @After
    public void destroy() {
        getSessionFactory().getCache().evictAllRegions();
        super.destroy();
    }

    @SuppressWarnings("ucnhecked")
    private List<Post> getLatestPosts(Session session) {
        return (List<Post>) session.createQuery(
            "select p " +
            "from Post p " +
            "order by p.createdOn desc")
            .setMaxResults(10)
            .setCacheable(true)
            .list();
    }



    @Test
    public void test2ndLevelCacheWithQuery() {
        doInTransaction(session -> {
            List<Post> posts = getLatestPosts(session);
        });

        doInTransaction(session -> {
            LOGGER.info("Check get entity is cached after query");
            Post post = (Post) session.get(Post.class, 1L);
        });

        doInTransaction(session -> {
            LOGGER.info("Check query entity is cached after query");
            List<Post> posts = getLatestPosts(session);
            Post post = posts.get(0);
            post.setName("High-Performance Hibernate");
            session.flush();

            LOGGER.info("Check query entity query is invalidated");
            posts = getLatestPosts(session);
        });
    }

    @Test
    public void test2ndLevelCacheWithQueryInvalidation() {
        doInTransaction(session -> {
            List<Post> posts = getLatestPosts(session);
        });

        doInTransaction(session -> {
            LOGGER.info("Insert a new Post!");

            Post newPost = new Post();
            newPost.setName("Hibernate Book!");
            session.persist(newPost);
            session.flush();

            LOGGER.info("Check query entity query is invalidated");
            List<Post> posts = getLatestPosts(session);
        });
    }

    @Entity(name = "Post")
    @Cacheable
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    public static class Post {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private String name;

        @Column(name = "created_on")
        @Temporal(TemporalType.TIMESTAMP)
        private Date createdOn = new Date();

        public Date getCreatedOn() {
            return createdOn;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
