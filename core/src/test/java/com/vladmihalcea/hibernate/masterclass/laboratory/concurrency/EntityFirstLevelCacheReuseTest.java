package com.vladmihalcea.hibernate.masterclass.laboratory.concurrency;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.Session;
import org.junit.Test;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;

/**
 * OptimisticLockingTest - Test to check optimistic checking
 *
 * @author Vlad Mihalcea
 */
public class EntityFirstLevelCacheReuseTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
            Product.class
        };
    }

    @Test
    public void testOptimisticLocking() {
        doInTransaction(session -> {
            Product product = new Product();
            product.setId(1L);
            product.setQuantity(7L);
            session.persist(product);
        });
        doInTransaction(session -> {
            final Product product = (Product) session.get(Product.class, 1L);
            try {
                executeSync( () -> doInTransaction(_session -> {
                    Product otherThreadProduct = (Product) _session.get(Product.class, 1L);
                    assertNotSame(product, otherThreadProduct);
                    otherThreadProduct.setQuantity(6L);
                }));
                Product reloadedProduct = (Product) session.createQuery("from product").uniqueResult();
                assertEquals(7L, reloadedProduct.getQuantity());
                assertEquals(6L,
                        ((Number) session
                                .createSQLQuery("select quantity from product where id = :id")
                                .setParameter("id", product.getId())
                                .uniqueResult())
                                .longValue()
                );
            } catch (Exception e) {
                fail(e.getMessage());
            }
        });
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
