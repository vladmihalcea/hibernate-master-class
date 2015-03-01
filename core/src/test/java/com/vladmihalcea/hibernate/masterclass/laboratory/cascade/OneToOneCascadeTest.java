package com.vladmihalcea.hibernate.masterclass.laboratory.cascade;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertFalse;


/**
 * OneToOneCascadeTest - Test to check @OneToOne Cascading
 *
 * @author Vlad Mihalcea
 */
public class OneToOneCascadeTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
                Post.class,
                PostDetails.class
        };
    }

    public Post newPost() {
        return doInTransaction(session -> {
            Post post = new Post();
            post.setName("Hibernate Master Class");

            PostDetails details = new PostDetails();

            post.addDetails(details);

            session.persist(post);

            return post;
        });
    }

    @Test
    public void testCascadeTypeMerge() {
        LOGGER.info("Test CascadeType.MERGE");

        Post post = newPost();
        post.setName("Hibernate Master Class Training Material");
        post.getDetails().setVisible(true);

        doInTransaction(session -> {
            session.merge(post);
        });
    }

    @Test
    public void testOrphanRemoval() {
        LOGGER.info("Test orphan removal");

        newPost();

        doInTransaction(session -> {
            Post post = (Post) session.get(Post.class, 1L);
            post.removeDetails();
        });
    }

    @Test
    public void testCascadeTypeDelete() {
        LOGGER.info("Test CascadeType.DELETE");

        newPost();

        doInTransaction(session -> {
            Post post = (Post) session.get(Post.class, 1L);
            session.delete(post);
        });
    }

    @Entity(name = "Post")
    public static class Post {

        @Id
        @GeneratedValue(strategy=GenerationType.IDENTITY)
        private Long id;

        private String name;

        @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
        private PostDetails details;

        @Version
        private int version;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }


        public PostDetails getDetails() {
            return details;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void addDetails(PostDetails details) {
            this.details = details;
            details.setPost(this);
        }

        public void removeDetails() {
            if (details != null) {
                details.setPost(null);
            }
            this.details = null;
        }
    }

    @Entity(name = "PostDetails")
    public static class PostDetails {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private Date createdOn;

        private boolean visible;

        @OneToOne
        @PrimaryKeyJoinColumn
        private Post post;

        @Version
        private int version;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Date getCreatedOn() {
            return createdOn;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public Post getPost() {
            return post;
        }

        public void setPost(Post post) {
            this.post = post;
        }

        @PrePersist
        public void onPersist() {
            createdOn = new Date();
        }
    }
}
