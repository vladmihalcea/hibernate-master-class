package com.vladmihalcea.hibernate.masterclass.laboratory.concurrency;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.Session;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    @org.junit.Test
    public void testOptimisticLocking() {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();

        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(Session session) {
                Product product = new Product();
                product.setId(123L);
                product.setQuantity(55L);
                session.persist(product);
                return null;
            }
        });

        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(Session session) {
                final Product product = (Product) session.get(Product.class, 123L);
                try {
                    executorService.submit(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            return doInTransaction(new TransactionCallable<Void>() {
                                @Override
                                public Void execute(Session _session) {
                                    Product otherThreadProduct = (Product) _session.get(Product.class, 123L);
                                    assertNotSame(product, otherThreadProduct);
                                    otherThreadProduct.setQuantity(66L);
                                    return null;
                                }
                            });
                        }
                    }).get();
                    //Product reloadedProduct = (Product) session.createQuery("from Product").list().get(0);
                    //assertEquals(66L, reloadedProduct.getQuantity());
                    assertEquals(66L, ((Number) session.createSQLQuery("select quantity from Product where id = 123").uniqueResult()).longValue());
                } catch (Exception e) {
                    fail(e.getMessage());
                }
                return null;
            }
        });
    }
}
