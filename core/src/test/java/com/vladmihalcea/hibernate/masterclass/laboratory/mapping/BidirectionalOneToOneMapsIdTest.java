package com.vladmihalcea.hibernate.masterclass.laboratory.mapping;

import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.junit.Test;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;

/**
 * <code>BidirectionalOneToOneMapsIdTest</code> - Bidirectional @OneToOne Test
 *
 * @author Vlad Mihalcea
 */
public class BidirectionalOneToOneMapsIdTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
            Post.class,
            PostDetails.class,
        };
    }

    @Test
    public void testLifecycle() {
        doInJPA(entityManager -> {
            Post post = new Post("First post");
            PostDetails details = new PostDetails("John Doe");
            post.setDetails(details);
            entityManager.persist(post);
        });

        doInJPA(entityManager -> {
            LOGGER.info("Fetching Post");
            Post post = entityManager.find(Post.class, 1L);
            /*Post post = entityManager.createQuery(
                    "select p " +
                            "from Post p " +
                            "where p.id = :id", Post.class)
                    .setParameter("id", 1L)
                    .getSingleResult();*/
        });
    }

    @Entity(name = "Post")
    @Table(name = "post")
    public static class Post {

        @Id
        @GeneratedValue
        private Long id;

        private String title;

        @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private PostDetails details;

        public Post() {}

        public Post(String title) {
            this.title = title;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public PostDetails getDetails() {
            return details;
        }

        public void setDetails(PostDetails details) {
            this.details = details;
            details.setPost(this);
        }
    }

    @Entity(name = "PostDetails")
    @Table(name = "post_details")
    public static class PostDetails {

        @Id
        private Long id;

        @Column(name = "created_on")
        private Date createdOn;

        @Column(name = "created_by")
        private String createdBy;

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "id")
        @MapsId
        private Post post;

        public PostDetails() {}

        public PostDetails(String createdBy) {
            createdOn = new Date();
            this.createdBy = createdBy;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Date getCreatedOn() {
            return createdOn;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public Post getPost() {
            return post;
        }

        public void setPost(Post post) {
            this.post = post;
        }
    }

    @Override
    protected boolean nativeHibernateSessionFactoryBootstrap() {
        return false;
    }
}
