package com.vladmihalcea.hibernate.masterclass.laboratory.concurrency;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.Session;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * AbstractEntityOptimisticLockingCollectionTest - Abstract Test to check optimistic locking collection versioning
 *
 * @author Vlad Mihalcea
 */
public abstract class AbstractEntityOptimisticLockingCollectionTest<P extends AbstractEntityOptimisticLockingCollectionTest.IPost<C>, C extends AbstractEntityOptimisticLockingCollectionTest.IComment<P>> extends AbstractTest {

    public interface IPost<C> {
        Long getId();

        void setId(Long id);

        public String getName();

        public void setName(String name);

        public List<C> getComments();

        public int getVersion();

        public void addComment(C comment);
    }

    public interface IComment<P> {

        String getReview();

        void setReview(String review);
    }

    private final Class<P> postClass;

    private final Class<C> commentClass;

    protected AbstractEntityOptimisticLockingCollectionTest(Class<P> postClass, Class<C> commentClass) {
        this.postClass = postClass;
        this.commentClass = commentClass;
    }

    protected void simulateConcurrentTransactions(final boolean shouldIncrementParentVersion) {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();

        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(Session session) {
                try {
                    P post = postClass.newInstance();
                    post.setId(1L);
                    post.setName("Hibernate training");
                    session.persist(post);
                    return null;
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });

        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(final Session session) {
                final P post = (P)
                        session.get(postClass, 1L);
                try {
                    executorService.submit(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            return doInTransaction(new TransactionCallable<Void>() {
                                @Override
                                public Void execute(Session _session) {
                                    try {
                                        P otherThreadPost = (P) _session.get(postClass, 1L);
                                        int loadTimeVersion = otherThreadPost.getVersion();
                                        assertNotSame(post, otherThreadPost);
                                        assertEquals(0L, otherThreadPost.getVersion());
                                        C comment = commentClass.newInstance();
                                        comment.setReview("Good post!");
                                        otherThreadPost.addComment(comment);
                                        _session.flush();
                                        if (shouldIncrementParentVersion) {
                                            assertEquals(otherThreadPost.getVersion(), loadTimeVersion + 1);
                                        } else {
                                            assertEquals(otherThreadPost.getVersion(), loadTimeVersion);
                                        }
                                        return null;
                                    } catch (Exception e) {
                                        throw new IllegalArgumentException(e);
                                    }
                                }
                            });
                        }
                    }).get();
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
                post.setName("Hibernate Master Class");
                session.flush();
                return null;
            }
        });
    }
}
