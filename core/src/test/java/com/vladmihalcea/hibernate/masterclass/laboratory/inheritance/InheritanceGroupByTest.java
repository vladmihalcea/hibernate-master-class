package com.vladmihalcea.hibernate.masterclass.laboratory.inheritance;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.Criteria;
import org.junit.Test;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * InheritanceGroupByTest - Inheritance GroupBy Test
 *
 * @author Vlad Mihalcea
 */
public class InheritanceGroupByTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
                FirmUser.class
        };
    }

    @Test
    public void testTree() {
        doInTransaction(session -> {
            session.save(new FirmUser());
            //List<FirmUser> result = session.createQuery("select distinct a from FirmUser a order by a.id").list();
            List<FirmUser> result1 = (List<FirmUser>) session.createQuery("from FirmUser order by id").list();
            List<Object[]> result2 = (List<Object[]>) session.createQuery("select distinct a, a.id from FirmUser a order by id").list();
            List<FirmUser> result3 = (List<FirmUser>) session.createSQLQuery(
                    "select * " +
                    "from FIRM_USER a " +
                    "LEFT JOIN BASE_USER b ON a.id = b.id " +
                    "order by a.id"
            )
            .addEntity("a", FirmUser.class)
            .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
            .list();
            assertEquals(1, result1.size());
            assertEquals(1, result2.size());
            assertEquals(1, result3.size());
        });
    }

    @MappedSuperclass
    public static abstract class GenericHierarchicalDictionary {

        public abstract GenericHierarchicalDictionary getParent();

        public abstract Set<? extends GenericHierarchicalDictionary> getChildren();

    }

    @MappedSuperclass
    public static abstract class BaseEntity implements java.io.Serializable {


        private Integer id;

        private Date createdDate = new Date();


        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        @Temporal(TemporalType.TIMESTAMP)
        @Column(name = "CREATED_DATE", nullable = true)
        public Date getCreatedDate() {
            return createdDate;
        }

        public void setCreatedDate(Date createdDate) {
            this.createdDate = createdDate;
        }

    }

    @Entity
    @Table(name = "BASE_USER")
    @Inheritance(strategy = InheritanceType.JOINED)
    @AttributeOverride(name = "id", column = @Column(name = "ID", nullable = false, insertable = false, updatable = false))
    public static abstract class BaseUser extends BaseEntity {


        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq")
        @SequenceGenerator(name = "seq", sequenceName = "USER_SEQ", allocationSize = 1)
        public Integer getId() {
            return super.getId();
        }

    }

    @Entity(name = "FirmUser")
    @Table(name = "FIRM_USER")
    public static class FirmUser extends BaseUser {

        private String name;

        @Column(name = "name")
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

}
