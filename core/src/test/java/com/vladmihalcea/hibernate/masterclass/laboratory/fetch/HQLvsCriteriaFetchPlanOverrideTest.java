package com.vladmihalcea.hibernate.masterclass.laboratory.fetch;

import com.vladmihalcea.hibernate.masterclass.laboratory.flushing.OrderLine;
import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * HQLvsCriteriaFetchPlanOverrideTest - Test HQL and Criteria fetch plan overriding capabilities
 *
 * @author Vlad Mihalcea
 */
public class HQLvsCriteriaFetchPlanOverrideTest  extends AbstractTest {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
            Parent.class,
            Child.class
        };
    }

    @Test
    public void testFetchPlan() {
        final Long parentId = doInTransaction(new TransactionCallable<Long>() {
            @Override
            public Long execute(Session session) {
                Parent parent = new Parent();
                Child son = new Child("Bob");
                Child daughter = new Child("Alice");
                parent.addChild(son);
                parent.addChild(daughter);
                session.persist(parent);
                session.flush();
                return parent.getId();
            }
        });

        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(Session session) {
                LOGGER.info("HQL override default fetch plan");

                List<Parent> parents = session.createQuery("select p from Parent p where p.id = :id")
                        .setParameter("id", parentId)
                        .list();
                assertEquals(1, parents.size());
                return null;
            }
        });

        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(Session session) {
                LOGGER.info("Criteria doesn't override default fetch plan");

                List<Parent> parents = session.createCriteria(Parent.class)
                        .add(Restrictions.eq("id", parentId))
                        .list();
                assertEquals(2, parents.size());
                return null;
            }
        });

    }

    @Entity(name = "Parent")
    @Table(name = "parent")
    public static class Parent {

        @Id
        @GeneratedValue(strategy= GenerationType.IDENTITY)
        private Long id;

        private String name;

        @OneToMany(mappedBy = "parent", cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
        private List<Child> children = new ArrayList<Child>();

        public Long getId() {
            return id;
        }

        public List<Child> getChildren() {
            return children;
        }

        public void addChild(Child child) {
            child.setParent(this);
            children.add(child);
        }
    }

    @Entity(name = "Child")
    @Table(name = "child")
    public static class Child {

        @Id
        @GeneratedValue(strategy=GenerationType.IDENTITY)
        private Long id;

        private String name;

        public Child() {
        }

        public Child(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @ManyToOne
        @JoinColumn(name = "parent_id")
        private Parent parent;

        public Parent getParent() {
            return parent;
        }

        public void setParent(Parent parent) {
            this.parent = parent;
        }
    }
}
