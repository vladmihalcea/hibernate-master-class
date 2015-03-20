package com.vladmihalcea.hibernate.masterclass.laboratory.cache;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractIntegrationTest;
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
public class SecondLevelCacheTest extends AbstractIntegrationTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
                Post.class,
                PostDetails.class,
                Comment.class
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

            post.addDetails(new PostDetails());
            post.addComment(new Comment("Good post!"));
            post.addComment(new Comment("Nice post!"));

            session.persist(post);
            session.get(PostDetails.class, post.getDetails().getId());
        });
    }

    @After
    public void destroy() {
        getSessionFactory().getCache().evictAllRegions();
        super.destroy();
    }

    @Test
    public void test2ndLevelCacheWithGet() {
        LOGGER.info("Test 2nd level cache");
        doInTransaction(session -> {
            Post post = (Post) session.get(Post.class, 1L);
        });
        doInTransaction(session -> {
            LOGGER.info("Check entity is cached after load");
            Post post = (Post) session.get(Post.class, 1L);
        });
    }

    @Test
    public void test2ndLevelCacheWithQuery() {
        LOGGER.info("Test 2nd level cache");
        doInTransaction(session -> {
            session.createSQLQuery("select * from Post").list();

            session.createQuery(
                    "select p " +
                            "from Post p " +
                            "where " +
                            "   p.id = :id").setParameter("id", 1L)
                    .list();

            Post post = (Post) session.createQuery(
                    "select p " +
                            "from Post p " +
                            "join fetch p.details " +
                            "join fetch p.comments " +
                            "where " +
                            "   p.id = :id").setParameter("id", 1L)
                    .setCacheable(true)
                    .uniqueResult();
        });
        doInTransaction(session -> {
            LOGGER.info("Check get entity is cached after query");
            Post post = (Post) session.get(Post.class, 1L);
        });

        doInTransaction(session -> {
            LOGGER.info("Check query entity is cached after query");
            Post post = (Post) session.createQuery(
                    "select p " +
                            "from Post p " +
                            "join fetch p.details " +
                            "join fetch p.comments " +
                            "where " +
                            "   p.id = :id").setParameter("id", 1L)
                    .setCacheable(true)
                    .uniqueResult();

            post.setName("High-Performance Hibernate");
            session.flush();

            LOGGER.info("Check query entity query is invalidated");
            post = (Post) session.createQuery(
                    "select p " +
                            "from Post p " +
                            "join fetch p.details " +
                            "where " +
                            "   p.id = :id").setParameter("id", 1L)
                    .setCacheable(true)
                    .uniqueResult();
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

        @OneToMany(cascade = CascadeType.ALL, mappedBy = "post")
        private List<Comment> comments = new ArrayList<>();

        @OneToOne(cascade = CascadeType.ALL, mappedBy = "post", optional = false)
        private PostDetails details;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Comment> getComments() {
            return comments;
        }

        public PostDetails getDetails() {
            return details;
        }

        public void addComment(Comment comment) {
            comments.add(comment);
            comment.setPost(this);
        }

        public void addDetails(PostDetails details) {
            this.details = details;
            details.setPost(this);
        }
    }

    @Entity(name = "PostDetails")
    @Cacheable
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    public static class PostDetails {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private Date createdOn;

        public PostDetails() {
            createdOn = new Date();
        }

        @ManyToOne
        @PrimaryKeyJoinColumn
        private Post post;

        public Long getId() {
            return id;
        }

        public void setPost(Post post) {
            this.post = post;
        }
    }

    @Entity(name = "Comment")
    @Cacheable
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    public static class Comment {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        @ManyToOne
        private Post post;

        public Comment() {
        }

        public Comment(String review) {
            this.review = review;
        }

        private String review;

        public Long getId() {
            return id;
        }

        public void setPost(Post post) {
            this.post = post;
        }

        public void setReview(String review) {
            this.review = review;
        }
    }
}
