package com.vladmihalcea.hibernate.masterclass.laboratory.mapping;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Immutable;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;

import static org.junit.Assert.assertEquals;


/**
 * OneTableMultipleEntitiesTest - Test to check the one table multiple entities
 *
 * @author Vlad Mihalcea
 */
public class OneTableMultipleEntitiesTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
                Post.class,
                PostSummary.class,
                UpdatablePostSummary.class
        };
    }

    @Before
    public void init() {
        super.init();
        doInTransaction(session -> {
            Post post = new Post();
            post.setName("Hibernate Master Class");
            post.setDescription("Hibernate Master Class Description");
            session.persist(post);
        });
    }

    @Test
    public void testOneTableMultipleEntities() {
        doInTransaction(session -> {
            Post post = (Post) session.get(Post.class, 1L);
            PostSummary postSummary = (PostSummary) session.get(PostSummary.class, 1L);
            UpdatablePostSummary updatablePostSummary = (UpdatablePostSummary) session.get(UpdatablePostSummary.class, 1L);
            assertEquals(post.getName(), postSummary.getName());
            assertEquals(post.getName(), updatablePostSummary.getName());
            updatablePostSummary.setName("Hibernate Master Class Tutorial.");
        });
    }

    @Entity(name = "Post")
    public static class Post {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;

        private String name;

        private String description;

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
    }

    @Entity(name = "PostSummary")
    @Table(name = "Post")
    @Immutable
    public static class PostSummary {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Entity(name = "UpdatablePostSummary")
    @Table(name = "Post")
    @DynamicUpdate
    public static class UpdatablePostSummary {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
