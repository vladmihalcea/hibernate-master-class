package com.vladmihalcea.hibernate.masterclass.laboratory.concurrency;

import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.dialect.lock.OptimisticEntityLockException;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

/**
 * LockModeOptimisticTest - Test to check LockMode.OPTIMISTIC
 *
 * @author Vlad Mihalcea
 */
public class LockModeOptimisticTest extends AbstractLockModeOptimisticTest {

    @Test
    public void testImplicitOptimisticLocking() {

        doInTransaction(session -> {
            final Product product = (Product) session.get(Product.class, 1L);
            try {
                executeSync(() -> doInTransaction(_session -> {
                    Product _product = (Product) _session.get(Product.class, 1L);
                    assertNotSame(product, _product);
                    _product.setPrice(BigDecimal.valueOf(14.49));
                }));
            } catch (Exception e) {
                fail(e.getMessage());
            }
            OrderLine orderLine = new OrderLine(product);
            session.persist(orderLine);
        });
    }

    @Test
    public void testExplicitOptimisticLocking() {

        try {
            doInTransaction(session -> {
                final Product product = (Product) session.get(Product.class, 1L, new LockOptions(LockMode.OPTIMISTIC));

                executeSync(() -> {
                    doInTransaction(_session -> {
                        Product _product = (Product) _session.get(Product.class, 1L);
                        assertNotSame(product, _product);
                        _product.setPrice(BigDecimal.valueOf(14.49));
                    });
                });

                OrderLine orderLine = new OrderLine(product);
                session.persist(orderLine);
            });
            fail("It should have thrown OptimisticEntityLockException!");
        } catch (OptimisticEntityLockException expected) {
            LOGGER.info("Failure: ", expected);
        }
    }
}
