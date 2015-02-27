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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


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

            PostDetails details = new PostDetails();
            details.setCreatedOn(new Date());

            Comment comment1 = new Comment();
            comment1.setReview("Good post!");
            Comment comment2 = new Comment();
            comment2.setReview("Nice post!");

            post.addDetails(details);
            post.addComment(comment1);
            post.addComment(comment2);

            session.persist(post);
        });
    }

    @Test
    public void testCascadeLockOnManagedEntity() throws InterruptedException {
        LOGGER.info("Test lock cascade for managed entity");
        doInTransaction(session -> {
            Post _post = (Post) session.createQuery(
                    "select p " +
                    "from Post p " +
                    "join fetch p.details " +
                    "where " +
                    "   p.id = :id"
            ).setParameter("id", 1L)
            .uniqueResult();
            session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_WRITE)).setScope(true).lock(_post);
        });
    }

    @Test
    public void testCascadeLockOnDetachedEntity() throws InterruptedException {
        LOGGER.info("Test lock cascade for detached entity");
        Post _post = doInTransaction(session -> (Post) session.get(Post.class, 1L));
        doInTransaction(session -> {
            assertFalse(session.contains(_post));
            session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_WRITE)).lock(_post);
        });
    }

    @Entity(name = "Post")
    public static class Post {

        @Id
        @GeneratedValue(strategy=GenerationType.IDENTITY)
        private Long id;

        private String name;

        @OneToMany(cascade = CascadeType.ALL, mappedBy = "post", orphanRemoval = true)
        private List<Comment> comments = new ArrayList<>();

        @OneToOne(cascade = CascadeType.ALL, mappedBy = "post", optional = false)
        private PostDetails details;

        @Version
        private int version;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

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

        public final int getVersion() {
            return version;
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
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private Date createdOn;

        @OneToOne
        @PrimaryKeyJoinColumn
        private Post post;

        @Version
        private int version;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Date getCreatedOn() {
            return createdOn;
        }

        public void setCreatedOn(Date createdOn) {
            this.createdOn = createdOn;
        }

        public Post getPost() {
            return post;
        }

        public void setPost(Post post) {
            this.post = post;
        }

        public final int getVersion() {
            return version;
        }

    }

    @Entity(name = "Comment")
    public static class Comment {

        @Id
        @GeneratedValue(strategy=GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        private Post post;

        private String review;

        public Long getId() {
            return id;
        }

        public Post getPost() {
            return post;
        }

        public void setPost(Post post) {
            this.post = post;
        }

        public String getReview() {
            return review;
        }

        public void setReview(String review) {
            this.review = review;
        }
    }
}
