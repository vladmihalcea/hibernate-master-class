package com.vladmihalcea.hibernate.masterclass.laboratory.batch;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractPostgreSQLIntegrationTest;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.dialect.Dialect;
import org.junit.Test;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * NoBatchingTest - Test to check the default batch support
 *
 * @author Vlad Mihalcea
 */
public class NoBatchingTest extends AbstractPostgreSQLIntegrationTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
            Post.class,
            PostDetails.class,
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
        LOGGER.info("{}.testInsert took {} millis",
                getClass().getSimpleName(),
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos));

        LOGGER.info("Test batch update");
        startNanos = System.nanoTime();

        doInTransaction(session -> {
            List<Post> posts = session.createQuery(
                "select distinct p " +
                "from Post p " +
                "join fetch p.comments c").list();

            for(Post post : posts) {
                post.title = "Blog " + post.title;
                for(Comment comment : post.comments) {
                    comment.review = "Blog " + comment.review;
                }
            }
            session.flush();
        });

        LOGGER.info("{}.testUpdate took {} millis",
                getClass().getSimpleName(),
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos));
    }

    @Test
    public void testCascadeDelete() {
        LOGGER.info("Test batch delete with cascade");
        final AtomicReference<Long> startNanos = new AtomicReference<>();
        addDeleteBatchingRows();
        doInTransaction(session -> {
            List<Post> posts = session.createQuery(
                "select distinct p " +
                "from Post p " +
                "join fetch p.details d " +
                "join fetch p.comments c")
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

    @Test
    public void testOrphanRemoval() {
        LOGGER.info("Test batch delete with orphan removal");
        final AtomicReference<Long> startNanos = new AtomicReference<>();
        addDeleteBatchingRows();
        doInTransaction(session -> {
            List<Post> posts = session.createQuery(
                "select distinct p " +
                "from Post p " +
                "join fetch p.details d " +
                "join fetch p.comments c")
            .list();
            startNanos.set(System.nanoTime());
            posts.forEach(Post::removeDetails);
            session.flush();
            posts.forEach(post -> {
                for (Iterator<Comment> commentIterator = post.getComments().iterator(); commentIterator.hasNext(); ) {
                    Comment comment =  commentIterator.next();
                    comment.post = null;
                    commentIterator.remove();
                }
            });
            session.flush();
            posts.forEach(session::delete);
        });
        LOGGER.info("{}.testOrphanRemoval took {} millis",
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

        @OneToMany(cascade = CascadeType.ALL, mappedBy = "post",
                orphanRemoval = true)
        private List<Comment> comments = new ArrayList<>();

        @OneToOne(cascade = CascadeType.ALL, mappedBy = "post",
                orphanRemoval = true, fetch = FetchType.LAZY)
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
