package com.vladmihalcea.hibernate.masterclass.laboratory.flushing;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractPostgreSQLIntegrationTest;
import org.junit.Test;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SqlCascadeDeleteBatchingTest - Test to check the SQL cascade delete
 *
 * @author Vlad Mihalcea
 */
public class MergeWithTransientOnInverseSideTest extends AbstractPostgreSQLIntegrationTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
            Post.class,
            Comment.class
        };
    }

    @Test
    public void testMergeDetached() {

        final Post post = doInTransaction(session -> {
            Post _post = new Post("Post");
            session.persist(_post);
            return _post;
        });

        doInTransaction(session -> {
            post.getComments().add(new Comment());
            session.merge(post);
        });
    }

    @Test
    public void testMergeTransient() {

        doInTransaction(session -> {
            Post _post = new Post("Post");
            _post.getComments().add(new Comment());
            session.persist(_post);
            return _post;
        });
    }

    @Entity(name = "Post")
    public static class Post {

        @Id
        @GeneratedValue
        private Long id;

        private String title;

        @Version
        private int version;

        private Post() {
        }

        public Post(String title) {
            this.title = title;
        }

        @OneToMany(mappedBy = "post")
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

    @Entity(name = "Comment")
    public static class Comment {

        @Id
        @GeneratedValue
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "post_id", nullable = false)
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
