package com.vladmihalcea.hibernate.masterclass.laboratory.flushing;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.Session;
import org.junit.Test;

import java.util.Date;

/**
 * AutoDirtyCheckingTest - Test to check auto dirty checking capabilities
 *
 * @author Vlad Mihalcea
 */
public class AutoDirtyCheckingTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
            OrderLine.class
        };
    }

    @Test
    public void testDirtyChecking() {
        doInTransaction(session -> {
            OrderLine orderLine = new OrderLine();
            session.persist(orderLine);
            session.flush();
            orderLine.setNumber(123L);
            orderLine.setOrderedBy("Vlad");
            orderLine.setOrderedOn(new Date());
            session.flush();
            orderLine.setOrderedBy("Alex");
        });
    }
}
