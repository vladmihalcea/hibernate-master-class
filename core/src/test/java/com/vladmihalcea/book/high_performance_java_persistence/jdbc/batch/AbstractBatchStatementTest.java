package com.vladmihalcea.book.high_performance_java_persistence.jdbc.batch;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractMySQLIntegrationTest;
import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractOracleXEIntegrationTest;
import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractPostgreSQLIntegrationTest;
import org.junit.Test;

import javax.persistence.*;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;

/**
 * AbstractBatchStatementTest - Base class for testing JDBC Statement batching
 *
 * @author Vlad Mihalcea
 */
public abstract class AbstractBatchStatementTest extends AbstractMySQLIntegrationTest {

    public static final String INSERT_POST = "insert into Post (title, version, id) values ('Post no. %1$d', 0, %1$d)";

    public static final String INSERT_POST_COMMENT = "insert into PostComment (post_id, review, version, id) values (%1$d, 'Post comment %2$d', 0, %2$d)";

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
                Post.class,
                PostDetails.class,
                Comment.class
        };
    }

    @Test
    public void testInsert() {
        LOGGER.info("Test batch insert");
        long startNanos = System.nanoTime();
        doInConnection(connection -> {
            try (Statement statement = connection.createStatement()) {
                int postCount = getPostCount();
                int postCommentCount = getPostCommentCount();

                for(int i = 0; i < postCount; i++) {
                    onStatement(statement, String.format(INSERT_POST, i));
                    for(int j = 0; j < postCommentCount; j++) {
                        onStatement(statement, String.format(INSERT_POST_COMMENT, i, (postCommentCount * i) + j));
                    }
                }
                onEnd(statement);
            } catch (SQLException e) {
                fail(e.getMessage());
            }
        });
        LOGGER.info("{}.testInsert took {} millis",
                getClass().getSimpleName(),
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos));
    }

    protected abstract void onStatement(Statement statement, String dml) throws SQLException;

    protected abstract void onEnd(Statement statement) throws SQLException;

    protected int getPostCount() {
        return 1000;
    }

    protected int getPostCommentCount() {
        return 5;
    }

    @Entity(name = "Post")
    public static class Post {

        @Id
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

    @Entity(name = "PostComment")
    public static class Comment {

        @Id
        private Long id;

        @ManyToOne
        private Post post;

        @Version
        private int version;

        private Comment() {
        }

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
