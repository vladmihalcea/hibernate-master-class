package com.vladmihalcea.hibernate.masterclass.laboratory.concurrency;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;


/**
 * CascadeLockTest - Test to check CascadeType.LOCK
 *
 * @author Vlad Mihalcea
 */
public class CascadeLockTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
                Post.class,
                PostDetails.class,
                Comment.class
        };
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
        });
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

    private void containsPost(Session session, Post post, boolean expected) {
        assertEquals(expected, session.contains(post));
        assertEquals(expected, (session.contains(post.getDetails())));
        for(Comment comment : post.getComments()) {
            assertEquals(expected, (session.contains(comment)));
        }
    }

    @Test
    public void testCascadeLockOnDetachedEntityWithoutScope() {
        LOGGER.info("Test lock cascade for detached entity without scope");

        //Load the Post entity, which will become detached
        Post post = doInTransaction(session -> (Post) session.createQuery(
                "select p " +
                "from Post p " +
                "join fetch p.details " +
                "join fetch p.comments " +
                "where " +
                "   p.id = :id"
        ).setParameter("id", 1L)
        .uniqueResult());

        //Change the detached entity state
        post.setName("Hibernate Training");
        doInTransaction(session -> {
            //The Post entity graph is detached
            containsPost(session, post, false);

            //The Lock request associates the entity graph and locks the requested entity
            session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_WRITE)).lock(post);

            //Hibernate doesn't know if the entity is dirty
            assertEquals("Hibernate Training", post.getName());

            //The Post entity graph is attached
            containsPost(session, post, true);
        });
        doInTransaction(session -> {
            //The detached Post entity changes have been lost
            Post _post = (Post) session.get(Post.class, 1L);
            assertEquals("Hibernate Master Class", _post.getName());
        });
    }

    @Test
    public void testCascadeLockOnDetachedEntityWithScope() {
        LOGGER.info("Test lock cascade for detached entity with scope");

        //Load the Post entity, which will become detached
        Post post = doInTransaction(session -> (Post) session.createQuery(
                "select p " +
                        "from Post p " +
                        "join fetch p.details " +
                        "join fetch p.comments " +
                        "where " +
                        "   p.id = :id"
        ).setParameter("id", 1L)
                .uniqueResult());

        //Change the detached entity state
        post.setName("Hibernate Training");
        doInTransaction(session -> {
            //The Post entity graph is detached
            containsPost(session, post, false);

            //The Lock request associates the entity graph and locks the requested entity
            session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_WRITE)).setScope(true).lock(post);

            //Hibernate doesn't know if the entity is dirty
            assertEquals("Hibernate Training", post.getName());

            //The Post entity graph is attached
            containsPost(session, post, true);
        });
        doInTransaction(session -> {
            //The detached Post entity changes have been lost
            Post _post = (Post) session.get(Post.class, 1L);
            assertEquals("Hibernate Master Class", _post.getName());
        });
    }

    @Test
    public void testUpdateOnDetachedEntity() {
        LOGGER.info("Test update for detached entity");
        //Load the Post entity, which will become detached
        Post post = doInTransaction(session -> (Post) session.createQuery(
                "select p " +
                        "from Post p " +
                        "join fetch p.details " +
                        "join fetch p.comments " +
                        "where " +
                        "   p.id = :id"
        ).setParameter("id", 1L)
        .uniqueResult());

        //Change the detached entity state
        post.setName("Hibernate Training");

        doInTransaction(session -> {
            //The Post entity graph is detached
            containsPost(session, post, false);

            //The update will trigger an entity state flush and attach the entity graph
            session.update(post);

            //The Post entity graph is attached
            containsPost(session, post, true);
        });
        doInTransaction(session -> {
            Post _post = (Post) session.get(Post.class, 1L);
            assertEquals("Hibernate Training", _post.getName());
        });
    }

    @Entity(name = "Post")
    public static class Post {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;

        private String name;

        @OneToMany(cascade = CascadeType.ALL, mappedBy = "post", orphanRemoval = true)
        private List<Comment> comments = new ArrayList<>();

        @OneToOne(cascade = CascadeType.ALL, mappedBy = "post", fetch = FetchType.LAZY)
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
    public static class PostDetails {

        @Id
        private Long id;

        private Date createdOn;

        public PostDetails() {
            createdOn = new Date();
        }

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "id")
        @MapsId
        private Post post;

        public Long getId() {
            return id;
        }

        public void setPost(Post post) {
            this.post = post;
        }
    }

    @Entity(name = "Comment")
    public static class Comment {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;

        @ManyToOne
        private Post post;

        public Comment() {}

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
