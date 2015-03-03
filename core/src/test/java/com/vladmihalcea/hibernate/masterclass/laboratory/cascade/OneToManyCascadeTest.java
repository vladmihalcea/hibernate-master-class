package com.vladmihalcea.hibernate.masterclass.laboratory.cascade;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.junit.Test;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


/**
 * OneToOneCascadeTest - Test to check @OneToOne Cascading
 *
 * @author Vlad Mihalcea
 */
public class OneToManyCascadeTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
                Post.class,
                Comment.class
        };
    }

    public Post newPost() {
        return doInTransaction(session -> {
            Post post = new Post();
            post.setName("Hibernate Master Class");

            Comment comment1 = new Comment();
            comment1.setReview("Good post!");
            Comment comment2 = new Comment();
            comment2.setReview("Nice post!");

            post.addComment(comment1);
            post.addComment(comment2);

            session.persist(post);

            return post;
        });
    }

    @Test
    public void testCascadeTypeMerge() {
        LOGGER.info("Test CascadeType.MERGE");

        Post post = newPost();
        post.setName("Hibernate Master Class Training Material");

        post.getComments()
                .stream()
                .filter(comment -> comment.getReview().toLowerCase().contains("nice"))
                .findAny()
                .ifPresent(comment -> comment.setReview("Keep up the good work!"));

        doInTransaction(session -> {
            session.merge(post);
        });
    }

    @Test
    public void testOrphanRemoval() {
        LOGGER.info("Test orphan removal");

        newPost();

        doInTransaction(session -> {
            Post post = (Post) session.createQuery(
                    "select p " +
                            "from Post p " +
                            "join fetch p.comments " +
                            "where p.id = :id")
                    .setParameter("id", 1L)
                    .uniqueResult();
            post.removeComment(post.getComments().get(0));
        });
    }

    @Test
    public void testCascadeTypeDelete() {
        LOGGER.info("Test CascadeType.DELETE");

        Post post = newPost();

        doInTransaction(session -> {
            session.delete(post);
        });
    }

    @Entity(name = "Post")
    public static class Post {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private String name;

        @OneToMany(cascade = CascadeType.ALL, mappedBy = "post", orphanRemoval = true)
        private List<Comment> comments = new ArrayList<>();

        public void setName(String name) {
            this.name = name;
        }

        public List<Comment> getComments() {
            return comments;
        }

        public void addComment(Comment comment) {
            comments.add(comment);
            comment.setPost(this);
        }

        public void removeComment(Comment comment) {
            comment.setPost(null);
            this.comments.remove(comment);
        }
    }

    @Entity(name = "Comment")
    public static class Comment {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        @ManyToOne
        private Post post;

        private String review;

        public void setPost(Post post) {
            this.post = post;
        }

        public String getReview() {
            return review;
        }

        public void setReview(String review) {
            this.review = review;
        }
    }

}
