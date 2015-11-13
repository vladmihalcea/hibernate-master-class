package com.vladmihalcea.hibernate.masterclass.laboratory.mapping;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

/**
 * <code>IndexOverrideTest</code> -
 *
 * @author Vlad Mihalcea
 */
public class IndexOverrideTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
                Base.class,
                ChildY.class,
                ChildZ.class
        };
    }

    @Before
    public void init() {
        super.init();
        doInTransaction(session -> {
            ChildY childy = new ChildY();
            ChildZ childz = new ChildZ();
            session.persist(childy);
            session.persist(childz);
        });
    }

    @Test
    public void testIndex() {
        doInTransaction(session -> {
            List<Base> bases = session.createQuery("from Base").list();
            bases.stream().forEach(base -> base.setX(UUID.randomUUID().toString()));
        });
    }

    @Entity(name = "Base")
    @Table(name="Base")
    @Inheritance(strategy = InheritanceType.JOINED)
    public static abstract class Base {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        @Transient
        protected String x;

        public Long getId() {
            return id;
        }

        public String getX() {
            return x;
        }

        public void setX(String x) {
            this.x = x;
        }
    }

    @Entity(name = "ChildY")
    @Table(name="ChildY")
    @DiscriminatorValue("Y")
    public static class ChildY extends Base {

        private String y;

        @Override
        @org.hibernate.annotations.Index(name = "xy")
        @Access(AccessType.PROPERTY)
        public String getX() {
            return x;
        }
    }

    @Entity(name = "ChildZ")
    @Table(name="ChildZ")
    @Inheritance(strategy = InheritanceType.JOINED)
    @DiscriminatorValue("Z")
    public static class ChildZ extends Base {

        private String z;

        @Override
        @org.hibernate.annotations.Index(name = "xz")
        @Access(AccessType.PROPERTY)
        public String getX() {
            return x;
        }
    }
}
