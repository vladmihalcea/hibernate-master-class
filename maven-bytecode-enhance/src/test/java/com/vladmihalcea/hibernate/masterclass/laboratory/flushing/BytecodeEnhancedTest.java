package com.vladmihalcea.hibernate.masterclass.laboratory.flushing;

import org.hibernate.Session;
import org.junit.Test;

import java.util.Date;

/**
 * BytecodeEnhancedTest - Test to check auto dirty checking capabilities
 *
 * @author Vlad Mihalcea
 */
public class BytecodeEnhancedTest extends AutoDirtyCheckingTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
            EnhancedOrderLine.class
        };
    }

    @Test
    public void testDirtyChecking() {
        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(Session session) {
                EnhancedOrderLine orderLine = new EnhancedOrderLine();
                session.persist(orderLine);
                session.flush();
                orderLine.setNumber(123L);
                orderLine.setOrderedBy("Vlad");
                orderLine.setOrderedOn(new Date());
                session.flush();
                orderLine.setOrderedBy("Alex");
                return null;
            }
        });
    }
}
