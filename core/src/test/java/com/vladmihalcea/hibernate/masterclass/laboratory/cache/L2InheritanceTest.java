package com.vladmihalcea.hibernate.masterclass.laboratory.cache;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.annotations.Proxy;
import org.junit.Test;

import javax.persistence.*;

import static org.junit.Assert.assertEquals;

/**
 * <code>L2InheritanceTest</code> - Test to prove the solution for http://stackoverflow.com/questions/32829276/hibernate-inheritance-and-l2-cache/32902873#32902873
 *
 * @author Vlad Mihalcea
 */
public class L2InheritanceTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
                EntityBase.class,
                EntityA.class,
                EntityB.class,
                HolderEntity.class,
        };
    }

    @Test
    public void testSequenceIdentifierGenerator() {
        doInTransaction(session -> {
            EntityA entityA = new EntityA();
            session.persist(entityA);
            HolderEntity holderEntity = new HolderEntity();
            holderEntity.setHoldedEntity(entityA);
            session.persist(holderEntity);
            session.flush();
        });

        doInTransaction(session -> {
            HolderEntity o = (HolderEntity) session.get(HolderEntity.class, 1);// loads from L2 cache
            assertEquals(EntityA.class, o.getHoldedEntity().getClass());
        });
    }

    @Entity(name = "EntityBase")
    @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
    @DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.STRING)
    @Proxy(lazy = false)
    static abstract class EntityBase {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        @Column
        private int id;
    }

    @Entity
    @DiscriminatorValue("EntityA")
    @Cacheable
    static class EntityA extends EntityBase {
        @Column
        private int aColumn;

        public int getAColumn() {
            return aColumn;
        }

        public void setAColumn(int aColumn) {
            this.aColumn = aColumn;
        }
    }

    @Entity
    @DiscriminatorValue("EntityB")
    @Cacheable
    static class EntityB extends EntityBase {
        @Column
        private int bColumn;

        public int getBColumn() {
            return bColumn;
        }

        public void setBColumn(int bColumn) {
            this.bColumn = bColumn;
        }
    }

    @Entity(name = "HolderEntity")
    @Cacheable
    static class HolderEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        @Column
        private int id;

        @ManyToOne(fetch = FetchType.LAZY)
        EntityBase holdedEntity;

        public EntityBase getHoldedEntity() {
            return holdedEntity;
        }

        public void setHoldedEntity(EntityBase holdedEntity) {
            this.holdedEntity = holdedEntity;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}