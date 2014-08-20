package com.vladmihalcea.hibernate.masterclass.laboratory.jpa;

import com.vladmihalcea.hibernate.masterclass.laboratory.flushing.OrderLine;
import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractJPATest;
import org.junit.Ignore;
import org.junit.Test;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.Properties;

/**
 * RuntimeProxyDirtyCheckingTest - Test to check runtime proxy dirty checking capabilities
 *
 * @author Vlad Mihalcea
 */
@Ignore
public class RuntimeProxyDirtyCheckingTest extends AbstractJPATest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
                OrderLine.class
        };
    }

    @Override
    protected Properties getProperties() {
        Properties properties = super.getProperties();
        properties.setProperty("hibernate.ejb.use_class_enhancer", Boolean.TRUE.toString());
        return properties;
    }

    @Test
    public void testAutoFlushHQL() {

        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(EntityManager entityManager) {
                OrderLine orderLine = new OrderLine();
                entityManager.persist(orderLine);
                entityManager.flush();
                orderLine.setNumber(123L);
                orderLine.setOrderedBy("Vlad");
                orderLine.setOrderedOn(new Date());
                entityManager.flush();
                return null;
            }
        });
    }
}
