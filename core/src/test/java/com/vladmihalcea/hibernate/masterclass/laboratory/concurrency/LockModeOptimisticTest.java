package com.vladmihalcea.hibernate.masterclass.laboratory.concurrency;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.dialect.lock.OptimisticEntityLockException;
import org.junit.Test;

import javax.persistence.*;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;

/**
 * LockModeOptimisticTest - Test to check LockMode.OPTIMISTIC
 *
 * @author Vlad Mihalcea
 */
public class LockModeOptimisticTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
            Product.class
        };
    }

    @Test
    public void testOptimisticLocking() {

        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(Session session) {
                Product product = new Product();
                product.setId(1L);
                product.setQuantity(7L);
                session.persist(product);
                return null;
            }
        });

        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(Session session) {
                final Product product = (Product) session.get(Product.class, 1L, new LockOptions(LockMode.NONE));
                try {
                    executeAndWait(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            return doInTransaction(new TransactionCallable<Void>() {
                                @Override
                                public Void execute(Session _session) {
                                    Product otherThreadProduct = (Product) _session.get(Product.class, 1L);
                                    assertNotSame(product, otherThreadProduct);
                                    otherThreadProduct.incrementLikes();
                                    return null;
                                }
                            });
                        }
                    });
                } catch (Exception e) {
                    fail(e.getMessage());
                }
                return null;
            }
        });

        try {
            doInTransaction(new TransactionCallable<Void>() {
                @Override
                public Void execute(Session session) {
                    final Product product = (Product) session.get(Product.class, 1L, new LockOptions(LockMode.OPTIMISTIC));
                    executeAndWait(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            return doInTransaction(new TransactionCallable<Void>() {
                                @Override
                                public Void execute(Session _session) {
                                    Product otherThreadProduct = (Product) _session.get(Product.class, 1L);
                                    assertNotSame(product, otherThreadProduct);
                                    otherThreadProduct.incrementLikes();
                                    return null;
                                }
                            });
                        }
                    });
                    return null;
                }
            });
        } catch (OptimisticEntityLockException e) {
            LOGGER.error("Optimistic locking failure", e);
        }
    }

    /**
     * Product - Product
     *
     * @author Vlad Mihalcea
     */
    @Entity(name = "product")
    @Table(name = "product")
    public static class Product {

        @Id
        private Long id;

        private long quantity;

        private int likes;

        @Version
        private int version;

        public Product() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public long getQuantity() {
            return quantity;
        }

        public void setQuantity(long quantity) {
            this.quantity = quantity;
        }

        public int getLikes() {
            return likes;
        }

        public int incrementLikes() {
            return ++likes;
        }
    }
}
