package com.vladmihalcea.hibernate.masterclass.laboratory.concurrency;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.junit.Test;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

/**
 * EntityOptimisticLockingOnUnidirectionalCollectionTest - Test to check optimistic locking on unidirectional collections
 *
 * @author Vlad Mihalcea
 */
public class EntityOptimisticLockingOnComponentCollectionTest extends AbstractTest {

    @Entity(name = "post")
    public static class Post {

        @Id
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
    }

    @Embeddable
    public static class Comment {

        @Column(name = "review")
        private String review;

        public String getReview() {
            return review;
        }

        public void setReview(String review) {
            this.review = review;
        }
    }

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
            Post.class
        };
    }

    @Test(expected = StaleObjectStateException.class)
    public void testOptimisticLocking() {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();

        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(Session session) {
                Post post = new Post();
                post.setId(1L);
                post.setName("Hibernate training");
                session.persist(post);
                return null;
            }
        });

        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(final Session session) {
                final Post post = (Post)
                        session.get(Post.class, 1L);
                try {
                    executorService.submit(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            return doInTransaction(new TransactionCallable<Void>() {
                                @Override
                                public Void execute(Session _session) {
                                    Post otherThreadPost = (Post) _session.get(Post.class, 1L);
                                    assertNotSame(post, otherThreadPost);
                                    assertEquals(0L, otherThreadPost.getVersion());
                                    Comment comment = new Comment();
                                    comment.setReview("Good post!");
                                    otherThreadPost.getComments().add(comment);
                                    _session.flush();
                                    assertEquals(1L, otherThreadPost.getVersion());
                                    return null;
                                }
                            });
                        }
                    }).get();
                } catch (InterruptedException e) {
                    fail(e.getMessage());
                } catch (ExecutionException e) {
                    fail(e.getMessage());
                }
                post.setName("Hibernate Master Class");
                session.flush();
                fail("Should throw optimistic locking exception");
                return null;
            }
        });
    }
}
