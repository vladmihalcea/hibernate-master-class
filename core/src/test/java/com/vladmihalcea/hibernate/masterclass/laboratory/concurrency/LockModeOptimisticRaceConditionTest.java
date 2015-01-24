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
public class LockModeOptimisticRaceConditionTest extends AbstractLockModeOptimisticRaceConditionTest {

    @Override
    protected void runRaceCondition(Work work) {
        LOGGER.info("Overwrite product price synchronously");
        Session _session = getSessionFactory().openSession();
        _session.doWork(work);
        _session.close();
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
                        raceConditionReady();
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

}
