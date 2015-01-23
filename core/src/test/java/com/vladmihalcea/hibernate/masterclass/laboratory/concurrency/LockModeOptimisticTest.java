package com.vladmihalcea.hibernate.masterclass.laboratory.concurrency;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.dialect.lock.OptimisticEntityLockException;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

/**
 * LockModeOptimisticTest - Test to check LockMode.OPTIMISTIC
 *
 * @author Vlad Mihalcea
 */
public class LockModeOptimisticTest extends AbstractTest {

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

    @Test
    public void testImplicitOptimisticLocking() {

        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(Session session) {
                final Product product = (Product) session.get(Product.class, 1L);
                try {
                    executeAndWait(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            return doInTransaction(new TransactionCallable<Void>() {
                                @Override
                                public Void execute(Session _session) {
                                    Product _product = (Product) _session.get(Product.class, 1L);
                                    assertNotSame(product, _product);
                                    _product.setPrice(BigDecimal.valueOf(14.49));
                                    return null;
                                }
                            });
                        }
                    });
                } catch (Exception e) {
                    fail(e.getMessage());
                }
                OrderLine orderLine = new OrderLine();
                orderLine.setProduct(product);
                session.persist(orderLine);
                return null;
            }
        });
    }

    @Test
    public void testExplicitOptimisticLocking() {

        try {
            doInTransaction(new TransactionCallable<Void>() {
                @Override
                public Void execute(Session session) {
                    final Product product = (Product) session.get(Product.class, 1L, new LockOptions(LockMode.OPTIMISTIC));

                    executeAndWait(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            return doInTransaction(new TransactionCallable<Void>() {
                                @Override
                                public Void execute(Session _session) {
                                    Product _product = (Product) _session.get(Product.class, 1L);
                                    assertNotSame(product, _product);
                                    _product.setPrice(BigDecimal.valueOf(14.49));
                                    return null;
                                }
                            });
                        }
                    });

                    OrderLine orderLine = new OrderLine();
                    orderLine.setProduct(product);
                    session.persist(orderLine);
                    return null;
                }
            });
            fail("It should have thrown OptimisticEntityLockException!");
        } catch (OptimisticEntityLockException expected) {
            LOGGER.info("Failure: ", expected);
        }
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
