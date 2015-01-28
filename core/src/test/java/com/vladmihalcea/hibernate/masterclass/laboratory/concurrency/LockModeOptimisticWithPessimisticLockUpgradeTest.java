package com.vladmihalcea.hibernate.masterclass.laboratory.concurrency;

import org.hibernate.*;
import org.hibernate.dialect.lock.OptimisticEntityLockException;
import org.hibernate.jdbc.Work;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * LockModeOptimisticWithPessimisticLockUpgradeTest - Test to check LockMode.OPTIMISTIC with pessimistic lock upgrade
 *
 * @author Vlad Mihalcea
 */
public class LockModeOptimisticWithPessimisticLockUpgradeTest extends LockModeOptimisticRaceConditionTest {

    @Override
    protected void lockUpgrade(Session session, Product product) {
        session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_READ)).lock(product);
    }

}
