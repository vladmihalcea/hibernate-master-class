package com.vladmihalcea.hibernate.masterclass.laboratory.testenv;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConnectionProviderTest {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private SessionFactory sf;

    @Before
    public void init() {
        sf = newSessionFactory();
    }

    @After
    public void destroy() {
        sf.close();
    }

    protected abstract SessionFactory newSessionFactory();

    public SessionFactory getSessionFactory() {
        return sf;
    }

    @Test
    public void test() {
        Session session = null;
        Transaction txn = null;
        try {
            session = sf.openSession();
            txn = session.beginTransaction();

            SecurityId securityId = new SecurityId();
            securityId.setRole("Role");
            session.persist(securityId);

            txn.commit();
        } catch (RuntimeException e) {
            if ( txn != null && txn.isActive() ) txn.rollback();
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
