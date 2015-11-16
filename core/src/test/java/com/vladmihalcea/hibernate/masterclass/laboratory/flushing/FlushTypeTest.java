package com.vladmihalcea.hibernate.masterclass.laboratory.flushing;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
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
    public void testAutoFlushHQL() {
        doInTransaction(session -> {
                Product product = new Product();
                session.persist(product);
                LOGGER.info("Check if Product is flushed when selecting Users using HQL");
                assertEquals(0L,  session.createQuery("select count(id) from User").uniqueResult());
                assertEquals(product.getId(), session.createQuery("select p.id from Product p").uniqueResult());
                return null;

        });
    }

    @Test
    public void testAutoFlushHQLSubSelect() {
        doInTransaction(session -> {
                Product product = new Product();
                product.setColor("Blue");
                session.persist(product);
                LOGGER.info("Check if Product is flushed, HQL + sub-select");
                assertEquals(0L,  session.createQuery(
                        "select count(*) " +
                        "from User u " +
                        "where u.favoriteColor in (select distinct(p.color) from Product p)"
                ).uniqueResult());
                return null;

        });
    }

    @Test
    public void testAutoFlushHQLThetaJoinSelect() {
        doInTransaction(session -> {
                Product product = new Product();
                product.setColor("Blue");
                session.persist(product);
                LOGGER.info("Check if Product is flushed, HQL + theta style join");
                assertEquals(0L,  session.createQuery(
                        "select count(*) " +
                                "from User u, Product p " +
                                "where u.favoriteColor = p.color"
                ).uniqueResult());
                return null;

        });
    }

    @Test
    public void testAutoFlushSQLQuery() {
        doInTransaction(session -> {
                Product product = new Product();
                session.persist(product);
                LOGGER.info("Check if Product is flushed when selecting Users using SQL");
                assertEquals(BigInteger.ZERO,  session.createSQLQuery("select count(id) from user").uniqueResult());
                assertNull(session.createSQLQuery("select id from product").uniqueResult());
                return null;

        });
    }

    @Test
    public void testAutoFlushSQLAlwaysFlush() {
        doInTransaction(session -> {
                Product product = new Product();
                session.persist(product);
                LOGGER.info("Check if Product is flushed when selecting Users using SQL with ALWAYS flush mode");
                assertEquals(BigInteger.ZERO,  session.createSQLQuery("select count(id) from user").uniqueResult());
                assertEquals(product.getId(), session.createSQLQuery("select id from product").setFlushMode(FlushMode.ALWAYS).uniqueResult());
                return null;

        });
    }

    @Test
    public void testAutoFlushSQLAddSynchronization() {
        doInTransaction(session -> {
                Product product = new Product();
                session.persist(product);
                LOGGER.info("Check if Product is flushed when selecting Users using SQL with synchronization");
                assertEquals(BigInteger.ZERO,  session.createSQLQuery("select count(id) from user").uniqueResult());
                assertEquals(product.getId(), session.createSQLQuery("select id from product").addSynchronizedEntityClass(Product.class).uniqueResult());
                return null;

        });
    }
    


    @Test
    public void testAutoFlushSQLNamedQuery() {
        doInTransaction(session -> {
                Product product = new Product();
                session.persist(product);
                LOGGER.info("Check if Product is flushed when selecting Users using SQL with named query");
                assertEquals(BigInteger.ZERO,  session.createSQLQuery("select count(id) from user").uniqueResult());
                assertNull(session.getNamedQuery("product_ids").uniqueResult());
                return null;
        });
    }
}
