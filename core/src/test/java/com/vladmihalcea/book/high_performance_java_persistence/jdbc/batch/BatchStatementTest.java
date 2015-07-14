package com.vladmihalcea.book.high_performance_java_persistence.jdbc.batch;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractPostgreSQLIntegrationTest;
import org.junit.Test;

import javax.persistence.*;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * BatchStatementTest - Test batching with Statements
 *
 * @author Vlad Mihalcea
 */
public class BatchStatementTest extends AbstractPostgreSQLIntegrationTest {

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
        doInConnection(connection -> {
            try (Statement statement = connection.createStatement()) {
                String INSERT_POST = "insert into Post (title, version, id) values ('Post no. %1$d', 0, %1$d)";
                String INSERT_POST_COMMENT = "insert into PostComment (post_id, review, version, id) values (%1$d, 'Post comment %2$d', 0, %2$d)";
                for(int i = 0; i < postCount(); i++) {
                    statement.addBatch(String.format(INSERT_POST, i));
                    for(int j = 0; j < postCommentCount(); j++) {
                        statement.addBatch(String.format(INSERT_POST_COMMENT, i, (postCommentCount() * i) + j));
                    }
                }
                int[] updateCount = statement.executeBatch();
                assertEquals(6000, updateCount.length);
                statement.clearBatch();
            } catch (SQLException e) {
                fail(e.getMessage());
            }
        });
    }

    protected int postCount() {
        return 1000;
    }

    protected int postCommentCount() {
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
