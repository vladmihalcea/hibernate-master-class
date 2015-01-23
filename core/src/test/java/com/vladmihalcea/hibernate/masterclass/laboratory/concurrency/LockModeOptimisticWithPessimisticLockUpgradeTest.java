package com.vladmihalcea.hibernate.masterclass.laboratory.concurrency;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.*;
import org.hibernate.dialect.lock.OptimisticEntityLockException;
import org.hibernate.jdbc.Work;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

/**
 * LockModeOptimisticWithPessimisticLockUpgradeTest - Test to check LockMode.OPTIMISTIC with pessimistic lock upgrade
 *
 * @author Vlad Mihalcea
 */
public class LockModeOptimisticWithPessimisticLockUpgradeTest extends AbstractTest {

    private AtomicBoolean overridePriceSync = new AtomicBoolean();
    private AtomicBoolean overridePriceAsync = new AtomicBoolean();

    final CountDownLatch startLatch = new CountDownLatch(1);
    final CountDownLatch endLatch = new CountDownLatch(2);

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
            Product.class,
            OrderLine.class
        };
    }



    @Before
    public void init() {
        super.init();
        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(Session session) {
                Product product = new Product();
                product.setId(1L);
                product.setDescription("USB Flash Drive");
                product.setPrice(BigDecimal.valueOf(12.99));
                session.persist(product);
                return null;
            }
        });
    }

    @Override
    protected Interceptor interceptor() {
        return new EmptyInterceptor() {
            @Override
            public void beforeTransactionCompletion(Transaction tx) {
                final Work work = new Work() {
                    @Override
                    public void execute(Connection connection) throws SQLException {
                        try(PreparedStatement ps = connection.prepareStatement("UPDATE product set price = 14.49 WHERE id = 1")) {
                            ps.executeUpdate();
                        }
                    }
                };
                if(overridePriceSync.get()) {
                    LOGGER.info("Overwrite product price synchronously");
                    Session _session = getSessionFactory().openSession();
                    _session.doWork(work);
                    _session.close();
                } else if(overridePriceAsync.get()) {
                    LOGGER.info("Overwrite product price asynchronously");
                    executeNoWait(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            Session _session = getSessionFactory().openSession();
                            _session.doWork(work);
                            endLatch.countDown();
                            _session.close();
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
                        OrderLine orderLine = new OrderLine();
                        orderLine.setProduct(product);
                        session.persist(orderLine);
                        overridePriceSync.set(true);
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

    @Test
    public void testExplicitOptimisticLockingWithPessimisticLockUpgrade() throws InterruptedException {

        try {
            doInTransaction(new TransactionCallable<Void>() {
                @Override
                public Void execute(Session session) {
                    try {
                        final Product product = (Product) session.get(Product.class, 1L, new LockOptions(LockMode.OPTIMISTIC));
                        OrderLine orderLine = new OrderLine();
                        orderLine.setProduct(product);
                        session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_READ)).lock(product);
                        session.persist(orderLine);
                        overridePriceAsync.set(true);
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                    return null;
                }
            });
        } catch (OptimisticEntityLockException expected) {
            LOGGER.info("Failure: ", expected);
        }
        endLatch.countDown();
        endLatch.await();
    }

    /**
     * Product - Product
     *
     * @author Vlad Mihalcea
     */
    @Entity(name = "product")
    @Table(name = "product")
    public static class Product {

        @Id
        private Long id;

        private String description;

        private BigDecimal price;

        @Version
        private int version;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }

    /**
     * OrderLine - Order Line
     *
     * @author Vlad Mihalcea
     */
    @Entity(name = "OrderLine")
    @Table(name = "order_line")
    public static class OrderLine {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        @ManyToOne
        private Product product;

        @Version
        private int version;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Product getProduct() {
            return product;
        }

        public void setProduct(Product product) {
            this.product = product;
        }
    }
}
