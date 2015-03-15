package com.vladmihalcea.hibernate.masterclass.laboratory.batch;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractIntegrationTest;
import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.dialect.Dialect;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * NoBatchingTest - Test to check the default batch support
 *
 * @author Vlad Mihalcea
 */
public class NoBatchingTest extends AbstractIntegrationTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
            Post.class,
            Comment.class
        };
    }

    @Test
    public void testInsertAndUpdate() {
        LOGGER.info("Test batch insert");
        long startNanos = System.nanoTime();
        doInTransaction(session -> {
            int batchSize = batchSize();
            for(int i = 0; i < itemsCount(); i++) {
                Post post = new Post(String.format("Post no. %d", i));
                int j = 0;
                post.addComment(new Comment(String.format("Post comment %d:%d", i, j++)));
                post.addComment(new Comment(String.format("Post comment %d:%d", i, j++)));
                post.addComment(new Comment(String.format("Post comment %d:%d", i, j++)));
                session.persist(post);
                if(i % batchSize == 0 && i > 0) {
                    session.flush();
                    session.clear();
                }
            }
        });
        LOGGER.info("{}.testInsert took {} millis", getClass().getSimpleName(), TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos));

        LOGGER.info("Test batch update");
        startNanos = System.nanoTime();

        doInTransaction(session -> {
            List<Post> posts = session.createQuery(
                "select distinct p " +
                "from Post p " +
                "join fetch p.comments c"
            ).list();

            for(Post post : posts) {
                post.title = "Blog " + post.title;
                for(Comment comment : post.comments) {
                    comment.review = "Blog " + comment.review;
                }
            }
            session.flush();
        });

        LOGGER.info("{}.testUpdate took {} millis", getClass().getSimpleName(), TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos));
    }

    protected int itemsCount() {
        return 100;
    }

    protected int batchSize() {
        return Integer.valueOf(Dialect.DEFAULT_BATCH_SIZE);
    }

    @Entity(name = "Post")
    @GenericGenerator(name = "sequenceGenerator", strategy = "enhanced-sequence",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled-lo"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "30")
            }
    )
    public static class Post {

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
        private Long id;

        private String title;

        @Version
        private int version;

        private Post() {
        }

        public Post(String title) {
            this.title = title;
        }

        @OneToMany(cascade = CascadeType.ALL, mappedBy = "post", orphanRemoval = true)
        private List<Comment> comments = new ArrayList<>();

        public void setTitle(String title) {
            this.title = title;
        }

        public void addComment(Comment comment) {
            comments.add(comment);
            comment.setPost(this);
        }
    }

    @Entity(name = "Comment")
    @GenericGenerator(name = "sequenceGenerator", strategy = "enhanced-sequence",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled-lo"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "30")
            }
    )
    public static class Comment {

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
        private Long id;

        @ManyToOne
        private Post post;

        @Version
        private int version;

        private Comment() {}

        public Comment(String review) {
            this.review = review;
        }

        private String review;

        public Long getId() {
            return id;
        }

        public void setPost(Post post) {
            this.post = post;
        }
    }
}
