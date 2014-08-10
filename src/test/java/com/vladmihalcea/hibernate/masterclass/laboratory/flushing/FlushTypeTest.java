package com.vladmihalcea.hibernate.masterclass.laboratory.flushing;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.junit.Test;

import java.util.UUID;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * FlushTypeTest - Test to prove flushing capabilities
 *
 * @author Vlad Mihalcea
 */
public class FlushTypeTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
            Product.class,
            User.class,
        };
    }

    @Override
    protected String[] packages() {
        return new String[] {
            getClass().getPackage().getName()
        };
    }

    @Test
    public void testAutoFlushHQLSubSelect() {
        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(Session session) {
                Product product = new Product("LCD");
                product.setColor("Blue");
                session.persist(product);
                LOGGER.info("Check if Product is flushed, HQL + sub-select");
                assertEquals(0, ((Number) session.createQuery(
                        "select count(*) " +
                        "from User u " +
                        "where u.favoriteColor in (select distinct(p.color) from Product p)"
                ).uniqueResult()).intValue());
                //assertEquals(1, ((Number) session.createQuery("select count(id) from Product").uniqueResult()).intValue());
                return null;
            }
        });
    }

    @Test
    public void testAutoFlushHQLThetaJoinSelect() {
        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(Session session) {
                Product product = new Product("LCD");
                product.setColor("Blue");
                session.persist(product);
                LOGGER.info("Check if Product is flushed, HQL + theta style join");
                assertEquals(0, ((Number) session.createQuery(
                        "select count(*) " +
                                "from User u, Product p " +
                                "where u.favoriteColor = p.color"
                ).uniqueResult()).intValue());
                //assertEquals(1, ((Number) session.createQuery("select count(id) from Product").uniqueResult()).intValue());
                return null;
            }
        });
    }

    @Test
    public void testAutoFlushNonClashingHQL() {
        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(Session session) {
                Product product = new Product("LCD");
                session.persist(product);
                LOGGER.info("Check if Product is flushed when selecting Users using HQL");
                assertEquals(0, ((Number) session.createQuery("select count(id) from User").uniqueResult()).intValue());
                assertEquals(1, ((Number) session.createQuery("select count(id) from Product").uniqueResult()).intValue());
                return null;
            }
        });
    }

    @Test
    public void testAutoFlushSQLQuery() {
        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(Session session) {
                Product product = new Product("LCD");
                session.persist(product);
                LOGGER.info("Check if Product is flushed when selecting Users using SQL");
                assertEquals(0, ((Number) session.createSQLQuery("select count(id) from user").uniqueResult()).intValue());
                assertNull(session.createSQLQuery("select id from product").uniqueResult());
                return null;
            }
        });
    }

    @Test
    public void testAutoFlushSQLAlwaysFlush() {
        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(Session session) {
                Product product = new Product("LCD");
                session.persist(product);
                LOGGER.info("Check if Product is flushed when selecting Users using SQL with ALWAYS flush mode");
                assertEquals(0, ((Number) session.createSQLQuery("select count(id) from user").uniqueResult()).intValue());
                assertEquals(product.getId(), session.createSQLQuery("select id from product").setFlushMode(FlushMode.ALWAYS).uniqueResult());
                return null;
            }
        });
    }

    @Test
    public void testAutoFlushSQLAddSynchronization() {
        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(Session session) {
                Product product = new Product("LCD");
                session.persist(product);
                LOGGER.info("Check if Product is flushed when selecting Users using SQL with synchronization");
                assertEquals(0, ((Number) session.createSQLQuery("select count(id) from user").uniqueResult()).intValue());
                assertEquals(product.getId(), session.createSQLQuery("select id from product").addSynchronizedEntityClass(Product.class).uniqueResult());
                return null;
            }
        });
    }

    @Test
    public void testAutoFlushSQLNamedQuery() {
        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(Session session) {
                Product product = new Product("LCD");
                session.persist(product);
                LOGGER.info("Check if Product is flushed when selecting Users using SQL with named query");
                assertEquals(0, ((Number) session.createSQLQuery("select count(id) from user").uniqueResult()).intValue());
                assertNull(session.getNamedQuery("product_ids").uniqueResult());
                return null;
            }
        });
    }
}
