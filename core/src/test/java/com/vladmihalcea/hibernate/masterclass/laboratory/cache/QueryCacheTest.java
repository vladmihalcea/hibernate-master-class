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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
                User.class,
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
            User user = new User("Vlad");
            session.persist(user);
            Post post = new Post("Hibernate Master Class", user);
            session.persist(post);
        });
    }

    @After
    public void destroy() {
        getSessionFactory().getCache().evictAllRegions();
        super.destroy();
    }

    @SuppressWarnings("unchecked")
    private List<Post> getLatestPosts(Session session) {
        return (List<Post>) session.createQuery(
            "select p " +
            "from Post p " +
            "order by p.createdOn desc")
            .setMaxResults(10)
            .setCacheable(true)
            .list();
    }

    @SuppressWarnings("unchecked")
    private List<Post> getLatestPostsByUserId(Session session) {
        return (List<Post>) session.createQuery(
            "select p " +
                    "from Post p " +
                    "where p.author.id = :authorId " +
                    "order by p.createdOn desc")
            .setParameter("authorId", 1L)
            .setMaxResults(10)
            .setCacheable(true)
            .list();
    }

    @SuppressWarnings("unchecked")
    private List<Post> getLatestPostsByUser(Session session) {
        User author = (User) session.get(User.class, 1L);
        return (List<Post>) session.createQuery(
                "select p " +
                        "from Post p " +
                        "where p.author = :author " +
                        "order by p.createdOn desc")
                .setParameter("author", author)
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

            LOGGER.info("Check query cache is invalidated");
            posts = getLatestPosts(session);
        });
    }

    @Test
    public void test2ndLevelCacheWithParameters() {
        doInTransaction(session -> {
            List<Post> posts = getLatestPostsByUserId(session);
            assertEquals(1, posts.size());
        });
        doInTransaction(session -> {
            List<Post> posts = getLatestPostsByUser(session);
            assertEquals(1, posts.size());
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

            LOGGER.info("Check query cache is invalidated");
            List<Post> posts = getLatestPosts(session);
        });
    }

    @Test
    public void test2ndLevelCacheWithNativeQueryInvalidation() {
        doInTransaction(session -> {
            List<Post> posts = getLatestPosts(session);
        });

        doInTransaction(session -> {
            LOGGER.info("Execute native query");

            assertEquals(1, session.createSQLQuery("update Author set name = '\"'||name||'\"' ").executeUpdate());

            LOGGER.info("Check query cache is invalidated");
            List<Post> posts = getLatestPosts(session);
        });
    }

    @Test
    public void test2ndLevelCacheWithNativeQuerySynchronization() {
        doInTransaction(session -> {
            List<Post> posts = getLatestPosts(session);
        });

        doInTransaction(session -> {
            LOGGER.info("Execute native query with synchronization");

            assertEquals(1, session
                    .createSQLQuery("update Author set name = '\"'||name||'\"' ")
                    .addSynchronizedEntityClass(User.class)
                    .executeUpdate());

            LOGGER.info("Check query cache is not invalidated");
            List<Post> posts = getLatestPosts(session);
        });
    }

    @Entity(name = "Author")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    public static class User {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private String name;

        public User() {
        }

        public User(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @Entity(name = "Post")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    public static class Post {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private String name;

        @Column(name = "created_on")
        @Temporal(TemporalType.TIMESTAMP)
        private Date createdOn = new Date();

        @ManyToOne
        private User author;

        public Post() {
        }

        public Post(String name, User author) {
            this.name = name;
            this.author = author;
        }

        public Date getCreatedOn() {
            return createdOn;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public User getAuthor() {
            return author;
        }
    }
}
