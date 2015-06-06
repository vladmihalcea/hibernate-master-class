package com.vladmihalcea.hibernate.masterclass.laboratory.cache;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractPostgreSQLIntegrationTest;
import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.Session;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * QueryCacheTest - Test to check the 2nd level query cache
 *
 * @author Vlad Mihalcea
 */
public class QueryCacheTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
                Post.class,
                Author.class,
        };
    }

    @Override
    protected Properties getProperties() {
        Properties properties = super.getProperties();
        properties.put("hibernate.cache.use_second_level_cache", Boolean.TRUE.toString());
        properties.put("hibernate.cache.region.factory_class", "org.hibernate.cache.ehcache.EhCacheRegionFactory");
        properties.put("hibernate.cache.use_query_cache", Boolean.TRUE.toString());
        return properties;
    }

    @Before
    public void init() {
        super.init();
        doInTransaction(session -> {
            Author author = new Author("Vlad");
            session.persist(author);
            Post post = new Post("Hibernate Master Class", author);
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
    private List<Post> getLatestPostsByAuthorId(Session session) {
        return (List<Post>) session.createQuery(
            "select p " +
            "from Post p " +
            "join p.author a " +
            "where a.id = :authorId " +
            "order by p.createdOn desc")
            .setParameter("authorId", 1L)
            .setMaxResults(10)
            .setCacheable(true)
            .list();
    }

    @SuppressWarnings("unchecked")
    private List<Post> getLatestPostsByAuthor(Session session) {
        Author author = (Author) session.get(Author.class, 1L);
        return (List<Post>) session.createQuery(
                "select p " +
                "from Post p " +
                "join p.author a " +
                "where a = :author " +
                "order by p.createdOn desc")
                .setParameter("author", author)
                .setMaxResults(10)
                .setCacheable(true)
                .list();
    }

    @Test
    public void test2ndLevelCacheWithQuery() {
        doInTransaction(session -> {
            LOGGER.info("Evict regions and run query");
            session.getSessionFactory().getCache().evictAllRegions();
            assertEquals(1, getLatestPosts(session).size());
        });

        doInTransaction(session -> {
            LOGGER.info("Check get entity is cached");
            Post post = (Post) session.get(Post.class, 1L);
        });

        doInTransaction(session -> {
            LOGGER.info("Check query is cached");
            assertEquals(1, getLatestPosts(session).size());
        });
    }

    @Test
    public void test2ndLevelCacheWithParameters() {
        doInTransaction(session -> {
            LOGGER.info("Query cache with basic type parameter");
            List<Post> posts = getLatestPostsByAuthorId(session);
            assertEquals(1, posts.size());
        });
        doInTransaction(session -> {
            LOGGER.info("Query cache with entity type parameter");
            List<Post> posts = getLatestPostsByAuthor(session);
            assertEquals(1, posts.size());
        });
    }

    @Test
    public void test2ndLevelCacheWithQueryInvalidation() {
        doInTransaction(session -> {
            Author author = (Author)
                session.get(Author.class, 1L);
            assertEquals(1, getLatestPosts(session).size());

            LOGGER.info("Insert a new Post");
            Post newPost = new Post("Hibernate Book", author);
            session.persist(newPost);
            session.flush();

            LOGGER.info("Query cache is invalidated");
            assertEquals(2, getLatestPosts(session).size());
        });

        doInTransaction(session -> {
            LOGGER.info("Check Query cache");
            assertEquals(2, getLatestPosts(session).size());
        });
    }

    @Test
    public void test2ndLevelCacheWithNativeQueryInvalidation() {
        doInTransaction(session -> {
            assertEquals(1, getLatestPosts(session).size());

            LOGGER.info("Execute native query");
            assertEquals(1, session.createSQLQuery(
                "update Author set name = '\"'||name||'\"' "
            ).executeUpdate());

            LOGGER.info("Check query cache is invalidated");
            assertEquals(1, getLatestPosts(session).size());
        });
    }

    @Test
    public void test2ndLevelCacheWithNativeQuerySynchronization() {
        doInTransaction(session -> {
            assertEquals(1, getLatestPosts(session).size());

            LOGGER.info("Execute native query with synchronization");
            assertEquals(1, session.createSQLQuery(
                    "update Author set name = '\"'||name||'\"' "
            ).addSynchronizedEntityClass(Author.class)
            .executeUpdate());

            LOGGER.info("Check query cache is not invalidated");
            assertEquals(1, getLatestPosts(session).size());
        });
    }

    @Entity(name = "Author")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    public static class Author {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private String name;

        public Author() {
        }

        public Author(String name) {
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

        @ManyToOne(fetch = FetchType.LAZY)
        private Author author;

        public Post() {
        }

        public Post(String name, Author author) {
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

        public Author getAuthor() {
            return author;
        }
    }
}
