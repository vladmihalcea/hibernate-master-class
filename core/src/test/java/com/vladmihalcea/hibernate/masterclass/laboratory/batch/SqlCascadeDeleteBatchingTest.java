package com.vladmihalcea.hibernate.masterclass.laboratory.batch;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractIntegrationTest;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.dialect.Dialect;
import org.junit.Test;

import javax.persistence.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * SqlCascadeDeleteBatchingTest - Test to check the SQL cascade delete
 *
 * @author Vlad Mihalcea
 */
public class SqlCascadeDeleteBatchingTest extends AbstractIntegrationTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
            Post.class,
            PostDetails.class,
            Comment.class
        };
    }

    @Override
    protected Properties getProperties() {
        Properties properties = super.getProperties();
        properties.put("hibernate.jdbc.batch_size", String.valueOf(batchSize()));
        properties.put("hibernate.order_inserts", "true");
        properties.put("hibernate.order_updates", "true");
        properties.put("hibernate.jdbc.batch_versioned_data", "true");
        return properties;
    }

    @Test
    public void testCascadeDelete() {
        LOGGER.info("Test delete with SQL cascade");
        final AtomicReference<Long> startNanos = new AtomicReference<>();
        addDeleteBatchingRows();
        doInTransaction(session -> {
            List<Post> posts = session.createQuery(
                "select p from Post p ")
            .list();
            startNanos.set(System.nanoTime());
            for (Post post : posts) {
                session.delete(post);
            }
        });
        LOGGER.info("{}.testCascadeDelete took {} millis",
                getClass().getSimpleName(),
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos.get()));
    }

    private void addDeleteBatchingRows() {
        doInTransaction(session -> {
            int batchSize = batchSize();
            for (int i = 0; i < itemsCount(); i++) {
                Post post = new Post(String.format("Post no. %d", i));
                int j = 0;
                post.addComment(new Comment(
                        String.format("Post comment %d:%d", i, j++)));
                post.addComment(new Comment(
                        String.format("Post comment %d:%d", i, j++)));
                post.addDetails(new PostDetails());
                session.persist(post);
                if(i % batchSize == 0 && i > 0) {
                    session.flush();
                    session.clear();
                }
            }
        });
    }

    protected int itemsCount() {
        return 3;
    }

    protected int batchSize() {
        return Integer.valueOf(Dialect.DEFAULT_BATCH_SIZE);
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

        @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE},
                mappedBy = "post")
        @OnDelete(action = OnDeleteAction.CASCADE)
        private List<Comment> comments = new ArrayList<>();

        @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE},
                mappedBy = "post", fetch = FetchType.LAZY)
        private PostDetails details;

        public void setTitle(String title) {
            this.title = title;
        }

        public List<Comment> getComments() {
            return comments;
        }

        public PostDetails getDetails() {
            return details;
        }

        public void addComment(Comment comment) {
            comments.add(comment);
            comment.setPost(this);
        }

        public void addDetails(PostDetails details) {
            this.details = details;
            details.setPost(this);
        }

        public void removeDetails() {
            this.details.setPost(null);
            this.details = null;
        }
    }

    @Entity(name = "PostDetails")
    public static class PostDetails {

        @Id
        private Long id;

        private Date createdOn;

        public PostDetails() {
            createdOn = new Date();
        }

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "id")
        @MapsId
        @OnDelete(action = OnDeleteAction.CASCADE)
        private Post post;

        public Long getId() {
            return id;
        }

        public void setPost(Post post) {
            this.post = post;
        }
    }

    @Entity(name = "Comment")
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

        @ManyToOne(fetch = FetchType.LAZY)
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
