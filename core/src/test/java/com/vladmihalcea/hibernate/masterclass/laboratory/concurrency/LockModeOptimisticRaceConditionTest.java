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

    private AtomicBoolean ready = new AtomicBoolean();
    private final CountDownLatch endLatch = new CountDownLatch(1);

    @Override
    protected Interceptor interceptor() {
        return new EmptyInterceptor() {
            @Override
            public void beforeTransactionCompletion(Transaction tx) {
                if(ready.get()) {
                    LOGGER.info("Overwrite product price asynchronously");

                    executeAsync(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            Session _session = getSessionFactory().openSession();
                            _session.doWork(new Work() {
                                @Override
                                public void execute(Connection connection) throws SQLException {
                                    try (PreparedStatement ps = connection.prepareStatement("UPDATE product set price = 14.49 WHERE id = 1")) {
                                        ps.executeUpdate();
                                    }
                                }
                            });
                            _session.close();
                            endLatch.countDown();
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
                        lockUpgrade(session, product);
                        ready.set(true);
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                    return null;
                }
            });
        } catch (OptimisticEntityLockException expected) {
            LOGGER.info("Failure: ", expected);
        }
        endLatch.await();
    }

    protected void lockUpgrade(Session session, Product product) {

    }

}
