package com.vladmihalcea.hibernate.masterclass.laboratory.batch;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractOracleXEIntegrationTest;
import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractPostgreSQLIntegrationTest;
import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.dialect.Dialect;
import org.junit.Test;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * NoFetchingTest - Test to check the default fetch support
 *
 * @author Vlad Mihalcea
 */
public class NoFetchingTest
        extends AbstractTest {
        //extends AbstractOracleXEIntegrationTest {
        //extends AbstractPostgreSQLIntegrationTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
            Post.class,
            Comment.class
        };
    }

    @Test
    public void testFetchSize() {
        doInTransaction(session -> {
            int batchSize = batchSize();
            for(int i = 0; i < itemsCount(); i++) {
                Post post = new Post(String.format("Post no. %d", i));
                int j = 0;
                post.addComment(new Comment(
                        String.format("Post comment %d:%d", i, j++)));
                post.addComment(new Comment(
                        String.format("Post comment %d:%d", i, j++)));
                session.persist(post);
                if(i % batchSize == 0 && i > 0) {
                    session.flush();
                    session.clear();
                }
            }
        });

        long startNanos = System.nanoTime();
        LOGGER.info("Test fetch size");
        doInTransaction(session -> {
            List posts = session.createQuery(
                    "select p " +
                            "from Post p " +
                            "join fetch p.comments ")
                .list();
            LOGGER.info("{}.fetched {} entities",
                    getClass().getSimpleName(),
                    posts.size());

        });
        LOGGER.info("{}.testFetch took {} millis",
                getClass().getSimpleName(),
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos));

    }

    protected int itemsCount() {
        return 10;
    }

    protected int batchSize() {
        return Integer.valueOf(Dialect.DEFAULT_BATCH_SIZE);
    }

    protected int fetchSize() {
        return 10;
    }

    @Entity(name = "Post")
    public static class Post {

        @Id
        @GenericGenerator(
            name = "sequenceGenerator",
            strategy = "enhanced-sequence",
            parameters = {
            @org.hibernate.annotations.Parameter(
                    name = "optimizer",
                    value = "pooled-lo"),
            @org.hibernate.annotations.Parameter(
                    name = "initial_value",
                    value = "1"),
            @org.hibernate.annotations.Parameter(
                    name = "increment_size",
                    value = "50"
            )
            }
        )
        @GeneratedValue(
                strategy = GenerationType.SEQUENCE,
                generator = "sequenceGenerator")
        private Long id;

        private String title;

        @Version
        private int version;

        private Post() {
        }

        public Post(String title) {
            this.title = title;
        }

        @OneToMany(cascade = CascadeType.ALL, mappedBy = "post",
                orphanRemoval = true)
        private List<Comment> comments = new ArrayList<>();

        public void setTitle(String title) {
            this.title = title;
        }

        public List<Comment> getComments() {
            return comments;
        }

        public void addComment(Comment comment) {
            comments.add(comment);
            comment.setPost(this);
        }
    }

    @Entity(name = "post_comment")
    public static class Comment {

        @Id
        @GenericGenerator(
            name = "sequenceGenerator",
            strategy = "enhanced-sequence",
            parameters = {
                @org.hibernate.annotations.Parameter(
                        name = "optimizer",
                        value = "pooled-lo"),
                @org.hibernate.annotations.Parameter(
                        name = "initial_value",
                        value = "1"),
                @org.hibernate.annotations.Parameter(
                        name = "increment_size",
                        value = "50"
                )
            }
        )
        @GeneratedValue(
                strategy = GenerationType.SEQUENCE,
                generator = "sequenceGenerator")
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
