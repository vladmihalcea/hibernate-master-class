package com.vladmihalcea.hibernate.masterclass.laboratory.concurrency;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.junit.Test;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertNotSame;

/**
 * EntityOptimisticLockingHighUpdateRateSingleEntityTest - Test to check optimistic checking on a single entity being updated by many threads
 *
 * @author Vlad Mihalcea
 */
public class EntityOptimisticLockingHighUpdateRateSingleEntityTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
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
                product.setName("TV");
                product.setDescription("Plasma TV");
                product.setPrice(BigDecimal.valueOf(199.99));
                product.setQuantity(7L);
                session.persist(product);
                return null;
            }
        });

        try {
            doInTransaction(new TransactionCallable<Void>() {
                @Override
                public Void execute(Session session) {
                    final Product product = (Product) session.get(Product.class, 1L);
                    executeAndWait(
                            new Callable<Void>() {
                                @Override
                                public Void call() throws Exception {
                                    return doInTransaction(new TransactionCallable<Void>() {
                                        @Override
                                        public Void execute(Session _session) {
                                            Product otherThreadProduct = (Product) _session.get(Product.class, 1L);
                                            assertNotSame(product, otherThreadProduct);
                                            otherThreadProduct.setQuantity(6L);
                                            return null;
                                        }
                                    });
                                }
                            }
                    );
                    product.incrementLikes();
                    session.flush();
                    throw new IllegalStateException("Should have thrown StaleObjectStateException!");
                }
            });
        } catch (StaleObjectStateException expected) {
            LOGGER.info("Optimistic locking failure", expected);
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

        @Column(unique = true, nullable = false)
        private String name;

        @Column(nullable = false)
        private String description;

        @Column(nullable = false)
        private BigDecimal price;

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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
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
