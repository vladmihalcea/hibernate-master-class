package com.vladmihalcea.hibernate.masterclass.laboratory.concurrency;

import org.hibernate.EmptyInterceptor;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.jdbc.Work;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AbstractLockModeOptimisticRaceConditionTest - Base Test to check LockMode.OPTIMISTIC race condition
 *
 * @author Vlad Mihalcea
 */
public abstract class AbstractLockModeOptimisticRaceConditionTest extends AbstractLockModeOptimisticTest {

    private AtomicBoolean ready = new AtomicBoolean();

    @Override
    protected Interceptor interceptor() {
        return new EmptyInterceptor() {
            @Override
            public void beforeTransactionCompletion(Transaction tx) {
                if(ready.get()) {
                    runRaceCondition(new Work() {
                        @Override
                        public void execute(Connection connection) throws SQLException {
                            try(PreparedStatement ps = connection.prepareStatement("UPDATE product set price = 14.49 WHERE id = 1")) {
                                ps.executeUpdate();
                            }
                        }
                    });
                }
            }
        };
    }

    protected abstract void runRaceCondition(Work work);

    protected void raceConditionReady() {
        ready.set(true);
    }

}
