package com.vladmihalcea.hibernate.masterclass.laboratory.fetch;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * HibernateApiFetchStrategyTest - Test HQL and Criteria fetch plan overriding capabilities
 *
 * @author Vlad Mihalcea
 */
public class HibernateApiMultiEagerCollectionFetchStrategyTest extends AbstractTest {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private Long productId;

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
                WarehouseProductInfo.class,
                Importer.class,
                Image.class,
                Product.class,
                Company.class,
                SubVersion.class,
                Version.class,
                Review.class,
        };
    }

    @Override
    public void init() {
        super.init();

        productId = doInTransaction(session -> {
                    Company company = new Company();
                    company.setName("TV Company");
                    session.persist(company);

                    Product product = new Product("tvCode");
                    product.setName("TV");
                    product.setCompany(company);

                    Image frontImage = new Image();
                    frontImage.setName("front image");
                    frontImage.setIndex(0);

                    Image sideImage = new Image();
                    sideImage.setName("side image");
                    sideImage.setIndex(1);

                    product.addImage(frontImage);
                    product.addImage(sideImage);

                    WarehouseProductInfo warehouseProductInfo = new WarehouseProductInfo();
                    warehouseProductInfo.setQuantity(101);
                    product.addWarehouse(warehouseProductInfo);

                    Importer importer = new Importer();
                    importer.setName("Importer");
                    session.persist(importer);
                    product.setImporter(importer);

                    Review review1 = new Review();
                    review1.setComment("Great product");

                    Review review2 = new Review();
                    review2.setComment("Sensational product");

                    product.addReview(review1);
                    product.addReview(review2);

                    session.persist(product);
                    return product.getId();
                }
        );
    }

    @Test
    public void testFetchChild() {
        doInTransaction(session -> {
            LOGGER.info("Fetch using find");
            Product product = (Product) session.get(Product.class, productId);
            assertNotNull(product);
            return null;

        });

        doInTransaction(session -> {
            LOGGER.info("Fetch using JPQL");
            Product product = (Product) session.createQuery(
                    "select p " +
                            "from Product p " +
                            "where p.id = :productId")
                    .setParameter("productId", productId)
                    .uniqueResult();
            assertNotNull(product);
            return null;

        });
        doInTransaction(session -> {

            LOGGER.info("Fetch using Criteria");

            Product product = (Product) session.createCriteria(Product.class)
                    .add(Restrictions.eq("id", productId))
                    .uniqueResult();
            assertNotNull(product);
            return null;

        });
        doInTransaction(session -> {

            LOGGER.info("Fetch list using Criteria");

            List products = session.createCriteria(Product.class)
                    .add(Restrictions.eq("id", productId))
                    .list();
            assertEquals(4, products.size());
            assertSame(products.get(0), products.get(1));
            return null;

        });
        doInTransaction(session -> {

                    LOGGER.info("Fetch distinct list using Criteria");

                    List products = session.createCriteria(Product.class)
                            .add(Restrictions.eq("id", productId))
                            .setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
                            .list();
                    assertEquals(1, products.size());
                    return null;
                }
        );

    }

    @Entity(name = "Company")
    public static class Company {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        @Column(unique = true, updatable = false)
        private String name;

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public int hashCode() {
            HashCodeBuilder hcb = new HashCodeBuilder();
            hcb.append(name);
            return hcb.toHashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Company)) {
                return false;
            }
            Company that = (Company) obj;
            EqualsBuilder eb = new EqualsBuilder();
            eb.append(name, that.name);
            return eb.isEquals();
        }

        @Override
        public String toString() {
            ToStringBuilder tsb = new ToStringBuilder(this);
            tsb.append("id", id);
            tsb.append("name", name);
            return tsb.toString();
        }
    }

    @Entity(name = "Image")
    public static class Image {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        @Column(updatable = false)
        private String name;

        @Column(unique = true)
        private int index;

        @ManyToOne(fetch = FetchType.LAZY)
        private Product product;

        @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "image", orphanRemoval = true)
        @OrderBy("type")
        private Set<Version> versions = new LinkedHashSet<Version>();

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getIndex() {
            return index;
        }

        public Product getProduct() {
            return product;
        }

        public void setProduct(Product product) {
            this.product = product;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public Set<Version> getVersions() {
            return versions;
        }

        public void addVersion(Version version) {
            versions.add(version);
            version.setImage(this);
        }

        public void removeVersion(Version version) {
            versions.remove(version);
            version.setImage(null);
        }

        @Override
        public int hashCode() {
            HashCodeBuilder hcb = new HashCodeBuilder();
            hcb.append(name);
            hcb.append(product);
            return hcb.toHashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Image)) {
                return false;
            }
            Image that = (Image) obj;
            EqualsBuilder eb = new EqualsBuilder();
            eb.append(name, that.name);
            eb.append(product, that.product);
            return eb.isEquals();
        }

        @Override
        public String toString() {
            ToStringBuilder tsb = new ToStringBuilder(this);
            tsb.append("id", id);
            tsb.append("name", name);
            tsb.append("index", index);
            tsb.append("product", product);
            return tsb.toString();
        }
    }

    @Entity(name = "Version")
    public static class Version {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        @Column
        private String type;

        @ManyToOne(fetch = FetchType.EAGER)
        private Image image;

        @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "version", orphanRemoval = true)
        private Set<SubVersion> subVersions = new LinkedHashSet<SubVersion>();

        public Long getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Image getImage() {
            return image;
        }

        public void setImage(Image image) {
            this.image = image;
        }

        public Set<SubVersion> getSubVersions() {
            return subVersions;
        }

        public void setSubVersions(Set<SubVersion> subVersions) {
            this.subVersions = subVersions;
        }

        public void addSubVersion(SubVersion subVersion) {
            subVersions.add(subVersion);
            subVersion.setVersion(this);
        }

        public void removeSubVersion(SubVersion subVersion) {
            subVersions.remove(subVersion);
            subVersion.setVersion(null);
        }

        @Override
        public int hashCode() {
            HashCodeBuilder hcb = new HashCodeBuilder();
            hcb.append(type);
            hcb.append(image);
            return hcb.toHashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Version)) {
                return false;
            }
            Version that = (Version) obj;
            EqualsBuilder eb = new EqualsBuilder();
            eb.append(type, that.getType());
            eb.append(image, that.getImage());
            return eb.isEquals();
        }

        @Override
        public String toString() {
            ToStringBuilder tsb = new ToStringBuilder(this);
            tsb.append("id", id);
            tsb.append("type", type);
            tsb.append("image", image);
            return tsb.toString();
        }
    }

    @Entity(name = "SubVersion")
    public static class SubVersion {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private String code;

        @ManyToOne(fetch = FetchType.EAGER)
        private Version version;

        public Long getId() {
            return id;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Version getVersion() {
            return version;
        }

        public void setVersion(Version version) {
            this.version = version;
        }

        @Override
        public int hashCode() {
            HashCodeBuilder hcb = new HashCodeBuilder();
            hcb.append(getCode());
            return hcb.toHashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof SubVersion)) {
                return false;
            }
            SubVersion that = (SubVersion) obj;
            EqualsBuilder eb = new EqualsBuilder();
            eb.append(getCode(), that.getCode());
            return eb.isEquals();
        }

        @Override
        public String toString() {
            ToStringBuilder tsb = new ToStringBuilder(this);
            tsb.append("id", id);
            tsb.append("code", getCode());
            return tsb.toString();
        }
    }

    @Entity(name = "Importer")
    public static class Importer {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private String name;

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public int hashCode() {
            HashCodeBuilder hcb = new HashCodeBuilder();
            hcb.append(name);
            return hcb.toHashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Importer)) {
                return false;
            }
            Importer that = (Importer) obj;
            EqualsBuilder eb = new EqualsBuilder();
            eb.append(name, that.getName());
            return eb.isEquals();
        }

        @Override
        public String toString() {
            ToStringBuilder tsb = new ToStringBuilder(this);
            tsb.append("id", id);
            tsb.append("name", name);
            return tsb.toString();
        }
    }

    @Entity(name = "review")
    public static class Review {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        @ManyToOne
        private Product product;

        private String comment;

        public Long getId() {
            return id;
        }

        public Product getProduct() {
            return product;
        }

        public void setProduct(Product product) {
            this.product = product;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        @Override
        public int hashCode() {
            HashCodeBuilder hcb = new HashCodeBuilder();
            hcb.append(comment);
            return hcb.toHashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Importer)) {
                return false;
            }
            Importer that = (Importer) obj;
            EqualsBuilder eb = new EqualsBuilder();
            eb.append(comment, that.getName());
            return eb.isEquals();
        }

        @Override
        public String toString() {
            ToStringBuilder tsb = new ToStringBuilder(this);
            tsb.append("id", id);
            tsb.append("comment", comment);
            return tsb.toString();
        }
    }

    @Entity(name = "Product")
    public static class Product {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private String name;

        @Column(updatable = false)
        private String code;

        private Integer quantity;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "company_id", nullable = false)
        private Company company;

        @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "product", optional = false)
        private WarehouseProductInfo warehouseProductInfo;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "importer_id")
        private Importer importer;

        @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "product", orphanRemoval = true)
        @OrderBy("index")
        private Set<Image> images = new LinkedHashSet<Image>();

        @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "product", orphanRemoval = true)
        private Set<Review> reviews = new LinkedHashSet<Review>();

        @javax.persistence.Version
        private int version;

        public Product() {
        }

        public Product(String code) {
            this.code = code;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public Company getCompany() {
            return company;
        }

        public void setCompany(Company company) {
            this.company = company;
        }

        public Set<Image> getImages() {
            return images;
        }

        public WarehouseProductInfo getWarehouseProductInfo() {
            return warehouseProductInfo;
        }

        public void setWarehouseProductInfo(WarehouseProductInfo warehouseProductInfo) {
            this.warehouseProductInfo = warehouseProductInfo;
        }

        public Importer getImporter() {
            return importer;
        }

        public void setImporter(Importer importer) {
            this.importer = importer;
        }

        public final int getVersion() {
            return version;
        }

        public void setImages(Set<Image> images) {
            this.images = images;
        }

        public void addImage(Image image) {
            images.add(image);
            image.setProduct(this);
        }

        public void removeImage(Image image) {
            images.remove(image);
            image.setProduct(null);
        }

        public void addReview(Review review) {
            reviews.add(review);
            review.setProduct(this);
        }

        public void addWarehouse(WarehouseProductInfo warehouseProductInfo) {
            warehouseProductInfo.setProduct(this);
            this.setWarehouseProductInfo(warehouseProductInfo);
        }

        @Override
        public int hashCode() {
            HashCodeBuilder hcb = new HashCodeBuilder();
            hcb.append(name);
            hcb.append(company);
            return hcb.toHashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Product)) {
                return false;
            }
            Product that = (Product) obj;
            EqualsBuilder eb = new EqualsBuilder();
            eb.append(name, that.getName());
            eb.append(company, that.getCompany());
            return eb.isEquals();
        }

        @Override
        public String toString() {
            ToStringBuilder tsb = new ToStringBuilder(this);
            tsb.append("id", id);
            tsb.append("name", name);
            tsb.append("version", version);
            return tsb.toString();
        }
    }

    @Entity(name = "WarehouseProductInfo")
    public static class WarehouseProductInfo {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private int quantity;

        @OneToOne(fetch = FetchType.EAGER)
        @PrimaryKeyJoinColumn
        private Product product;

        public Long getId() {
            return id;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public Product getProduct() {
            return product;
        }

        public void setProduct(Product product) {
            this.product = product;
        }

        @Override
        public int hashCode() {
            HashCodeBuilder hcb = new HashCodeBuilder();
            hcb.append(product);
            return hcb.toHashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof WarehouseProductInfo)) {
                return false;
            }
            WarehouseProductInfo that = (WarehouseProductInfo) obj;
            EqualsBuilder eb = new EqualsBuilder();
            eb.append(product, that.getProduct());
            return eb.isEquals();
        }

        @Override
        public String toString() {
            ToStringBuilder tsb = new ToStringBuilder(this);
            tsb.append("id", id);
            tsb.append("name", quantity);
            tsb.append("product", product);
            return tsb.toString();
        }
    }

}
