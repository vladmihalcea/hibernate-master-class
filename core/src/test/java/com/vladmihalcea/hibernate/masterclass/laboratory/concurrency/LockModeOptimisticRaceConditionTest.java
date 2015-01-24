package com.vladmihalcea.hibernate.masterclass.laboratory.concurrency;

import org.hibernate.*;
import org.hibernate.dialect.lock.OptimisticEntityLockException;
import org.hibernate.jdbc.Work;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * LockModeOptimisticWithPessimisticLockUpgradeTest - Test to check LockMode.OPTIMISTIC with pessimistic lock upgrade
 *
 * @author Vlad Mihalcea
 */
public class LockModeOptimisticRaceConditionTest extends AbstractLockModeOptimisticTest {

    private AtomicBoolean overridePriceSync = new AtomicBoolean();
    private AtomicBoolean overridePriceAsync = new AtomicBoolean();

    final CountDownLatch endLatch = new CountDownLatch(2);

    @Override
    protected Interceptor interceptor() {
        return new EmptyInterceptor() {
            @Override
            public void beforeTransactionCompletion(Transaction tx) {
                final Work work = new Work() {
                    @Override
                    public void execute(Connection connection) throws SQLException {
                        try(PreparedStatement ps = connection.prepareStatement("UPDATE product set price = 14.49 WHERE id = 1")) {
                            ps.executeUpdate();
                        }
                    }
                };
                if(overridePriceSync.get()) {
                    LOGGER.info("Overwrite product price synchronously");
                    Session _session = getSessionFactory().openSession();
                    _session.doWork(work);
                    _session.close();
                } else if(overridePriceAsync.get()) {
                    LOGGER.info("Overwrite product price asynchronously");
                    executeNoWait(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            Session _session = getSessionFactory().openSession();
                            _session.doWork(work);
                            endLatch.countDown();
                            _session.close();
                            return null;
                        }
                    });
                    try {
                        LOGGER.info("Wait 500 ms for lock to be acquired!");
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        };
    }

    @Test
    public void testExplicitOptimisticLocking() throws InterruptedException {
        try {
            doInTransaction(new TransactionCallable<Void>() {
                @Override
                public Void execute(Session session) {
                    try {
                        final Product product = (Product) session.get(Product.class, 1L, new LockOptions(LockMode.OPTIMISTIC));
                        OrderLine orderLine = new OrderLine(product);
                        session.persist(orderLine);
                        overridePriceSync.set(true);
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                    return null;
                }
            });
        } catch (OptimisticEntityLockException expected) {
            LOGGER.info("Failure: ", expected);
        }
    }

    @Test
    public void testExplicitOptimisticLockingWithPessimisticLockUpgrade() throws InterruptedException {

        try {
            doInTransaction(new TransactionCallable<Void>() {
                @Override
                public Void execute(Session session) {
                    try {
                        final Product product = (Product) session.get(Product.class, 1L, new LockOptions(LockMode.OPTIMISTIC));
                        OrderLine orderLine = new OrderLine(product);
                        session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_READ)).lock(product);
                        session.persist(orderLine);
                        overridePriceAsync.set(true);
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                    return null;
                }
            });
        } catch (OptimisticEntityLockException expected) {
            LOGGER.info("Failure: ", expected);
        }
        endLatch.countDown();
        endLatch.await();
    }

}
