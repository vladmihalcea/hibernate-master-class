package com.vladmihalcea.hibernate.masterclass.laboratory.concurrency;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.dialect.lock.OptimisticEntityLockException;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.concurrent.Callable;

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

        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(Session session) {
                final Product product = (Product) session.get(Product.class, 1L);
                try {
                    executeAndWait(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            return doInTransaction(new TransactionCallable<Void>() {
                                @Override
                                public Void execute(Session _session) {
                                    Product _product = (Product) _session.get(Product.class, 1L);
                                    assertNotSame(product, _product);
                                    _product.setPrice(BigDecimal.valueOf(14.49));
                                    return null;
                                }
                            });
                        }
                    });
                } catch (Exception e) {
                    fail(e.getMessage());
                }
                OrderLine orderLine = new OrderLine(product);
                session.persist(orderLine);
                return null;
            }
        });
    }

    @Test
    public void testExplicitOptimisticLocking() {

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
                                    Product _product = (Product) _session.get(Product.class, 1L);
                                    assertNotSame(product, _product);
                                    _product.setPrice(BigDecimal.valueOf(14.49));
                                    return null;
                                }
                            });
                        }
                    });

                    OrderLine orderLine = new OrderLine(product);
                    session.persist(orderLine);
                    return null;
                }
            });
            fail("It should have thrown OptimisticEntityLockException!");
        } catch (OptimisticEntityLockException expected) {
            LOGGER.info("Failure: ", expected);
        }
    }
}
