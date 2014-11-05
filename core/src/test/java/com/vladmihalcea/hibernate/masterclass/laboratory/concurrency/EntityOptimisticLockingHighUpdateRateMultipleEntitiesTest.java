package com.vladmihalcea.hibernate.masterclass.laboratory.concurrency;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.junit.Test;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

/**
 * EntityOptimisticLockingHighUpdateRateSingleEntityTest - Test to check optimistic checking on a single entity being updated by many threads
 *
 * @author Vlad Mihalcea
 */
public class EntityOptimisticLockingHighUpdateRateMultipleEntitiesTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
                Product.class,
                ProductStock.class,
                ProductLiking.class
        };
    }

    @Test
    public void testOptimisticLocking() {

        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(Session session) {
                Product product = Product.newInstance();
                product.setId(1L);
                product.setName("TV");
                product.setDescription("Plasma TV");
                product.setPrice(BigDecimal.valueOf(199.99));
                product.setQuantity(7L);
                session.persist(product);
                return null;
            }
        });

        try {
            doInTransaction(new TransactionCallable<Void>() {
                @Override
                public Void execute(Session session) {
                    final Product product = (Product) session.get(Product.class, 1L);
                    executeAndWait(
                            new Callable<Void>() {
                                @Override
                                public Void call() throws Exception {
                                    return doInTransaction(new TransactionCallable<Void>() {
                                        @Override
                                        public Void execute(Session _session) {
                                            Product otherThreadProduct = (Product) _session.get(Product.class, 1L);
                                            assertNotSame(product, otherThreadProduct);
                                            otherThreadProduct.setQuantity(6L);
                                            return null;
                                        }
                                    });
                                }
                            }
                    );
                    product.incrementLikes();
                    session.flush();
                    return null;
                }
            });
        } catch (StaleObjectStateException e) {
            fail(e.getMessage());
        }
    }

    /**
     * ProductStock - Product Stock
     *
     * @author Vlad Mihalcea
     */
    @Entity(name = "ProductStock")
    @Table(name = "product_stock")
    public static class ProductStock {

        @Id
        private Long id;

        @MapsId
        @OneToOne
        private Product product;

        private long quantity;

        public Long getId() {
            return id;
        }

        public Product getProduct() {
            return product;
        }

        public void setProduct(Product product) {
            this.product = product;
        }

        public long getQuantity() {
            return quantity;
        }

        public void setQuantity(long quantity) {
            this.quantity = quantity;
        }
    }

    /**
     * ProductStock - Product Stock
     *
     * @author Vlad Mihalcea
     */
    @Entity(name = "ProductLiking")
    @Table(name = "product_liking")
    public static class ProductLiking {

        @Id
        private Long id;

        @MapsId
        @OneToOne
        private Product product;

        private int likes;

        public Long getId() {
            return id;
        }

        public Product getProduct() {
            return product;
        }

        public void setProduct(Product product) {
            this.product = product;
        }

        public int getLikes() {
            return likes;
        }

        public int incrementLikes() {
            return ++likes;
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

        public static Product newInstance() {
            Product product = new Product();
            ProductStock stock = new ProductStock();
            stock.setProduct(product);
            product.stock = stock;
            ProductLiking liking = new ProductLiking();
            liking.setProduct(product);
            product.liking = liking;
            return product;
        }

        @Id
        private Long id;

        @Column(unique = true, nullable = false)
        private String name;

        @Column(nullable = false)
        private String description;

        @Column(nullable = false)
        private BigDecimal price;

        @OneToOne(optional = false, mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
        @PrimaryKeyJoinColumn
        private ProductStock stock;

        @OneToOne(optional = false, mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
        @PrimaryKeyJoinColumn
        private ProductLiking liking;

        @Version
        private int version;

        public Product() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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

        public long getQuantity() {
            return stock.getQuantity();
        }

        public void setQuantity(long quantity) {
            stock.setQuantity(quantity);
        }

        public int getLikes() {
            return liking.getLikes();
        }

        public int incrementLikes() {
            return liking.incrementLikes();
        }
    }
}
