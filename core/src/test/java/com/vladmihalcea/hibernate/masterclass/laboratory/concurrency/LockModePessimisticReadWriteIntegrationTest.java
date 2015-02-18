package com.vladmihalcea.hibernate.masterclass.laboratory.concurrency;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractIntegrationTest;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;


/**
 * LockModePessimisticReadWriteIntegrationTest - Test to check LockMode.PESSIMISTIC_READ and LockMode.PESSIMISTIC_WRITE
 *
 * @author Vlad Mihalcea
 */
public class LockModePessimisticReadWriteIntegrationTest extends AbstractIntegrationTest {

    public static final int WAIT_MILLIS = 500;

    private final CountDownLatch startLatch = new CountDownLatch(1);
    private final CountDownLatch endLatch = new CountDownLatch(1);

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
                Product.class
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

    @Test
    public void testConcurrentPessimisticForceIncrementLockingFailFast() throws InterruptedException {
        LOGGER.info("Test Concurrent PESSIMISTIC_FORCE_INCREMENT Lock Mode fail fast");
        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(Session session) {
                try {
                    Product product = (Product) session.get(Product.class, 1L);
                    session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_READ)).lock(product);
                    LOGGER.info("PESSIMISTIC_READ acquired");

                    executeNoWait(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            return doInTransaction(new TransactionCallable<Void>() {
                                @Override
                                public Void execute(Session _session) {
                                    try {
                                        startLatch.await();
                                        Product _product = (Product) _session.get(Product.class, 1L);
                                        _session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_WRITE)).lock(_product);
                                        LOGGER.info("PESSIMISTIC_WRITE acquired");
                                        endLatch.countDown();
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }
                                    return null;
                                }
                            });
                        }
                    });
                    try {
                        LOGGER.info("Wait {} ms for lock to be acquired!", WAIT_MILLIS);
                        startLatch.countDown();
                        Thread.sleep(WAIT_MILLIS);
                    } catch (InterruptedException e) {
                        throw new IllegalStateException(e);
                    }

                } catch (StaleObjectStateException expected) {
                    LOGGER.info("Failure: ", expected);
                }
                return null;
            }
        });
        try {
            endLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Product - Product
     *
     * @author Vlad Mihalcea
     */
    @Entity(name = "Product")
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
}
