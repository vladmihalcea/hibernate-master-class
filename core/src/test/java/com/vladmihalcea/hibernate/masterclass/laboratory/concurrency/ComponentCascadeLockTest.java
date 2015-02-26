package com.vladmihalcea.hibernate.masterclass.laboratory.concurrency;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractIntegrationTest;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


/**
 * CascadeLockTest - Test to check CascadeType.LOCK
 *
 * @author Vlad Mihalcea
 */
public class ComponentCascadeLockTest extends AbstractIntegrationTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
                Post.class,
                Comment.class
        };
    }

    @Before
    public void init() {
        super.init();
        doInTransaction(session -> {
            Post post = new Post();
            post.setName("Hibernate Master Class");
            Comment comment1 = new Comment();
            comment1.setReview("Good post!");
            Comment comment2 = new Comment();
            comment2.setReview("Nice post!");
            post.getComments().add(comment1);
            post.getComments().add(comment2);
            session.persist(post);
        });
    }

    @Test
    public void testLockOnCascade() throws InterruptedException {
        LOGGER.info("Test cascade");
        doInTransaction(session -> {
            Post post = (Post) session.get(Post.class, 1L);
            session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_WRITE)).setScope(true).lock(post);
        });
    }

    @Entity(name = "post")
    public static class Post implements AbstractEntityOptimisticLockingCollectionTest.IPost<Comment> {

        @Id
        @GeneratedValue(strategy=GenerationType.IDENTITY)
        private Long id;

        private String name;

        @ElementCollection
        @JoinTable(name = "post_comments", joinColumns = @JoinColumn(name = "post_id"))
        @OrderColumn(name = "comment_index")
        private List<Comment> comments = new ArrayList<Comment>();

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

        public final int getVersion() {
            return version;
        }

        public void addComment(Comment comment) {
            comments.add(comment);
        }
    }

    @Embeddable
    public static class Comment implements AbstractEntityOptimisticLockingCollectionTest.IComment<Post> {

        private String review;

        public String getReview() {
            return review;
        }

        public void setReview(String review) {
            this.review = review;
        }
    }
}
