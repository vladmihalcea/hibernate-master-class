package com.vladmihalcea.hibernate.masterclass.laboratory.cascade;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.annotations.Immutable;
import org.junit.Test;

import javax.persistence.*;
import java.util.Date;


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
                PostDetails.class,
                Commit.class,
                BranchMerge.class
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
        Post post = newPost();

        doInTransaction(session -> {
            session.delete(post);
        });
    }

    @Test
    public void testCascadeForUnidirectionalAssociation() {
        LOGGER.info("Test Cascade for unidirectional");

        doInTransaction(session -> {
            Commit commit = new Commit("Reintegrate feature branch FP-123");
            commit.addBranchMerge(
                    "FP-123",
                    "develop"
            );
            session.persist(commit);
        });

        doInTransaction(session -> {
            Commit commit = (Commit) session.get(Commit.class, 1L);
            session.delete(commit);
        });
    }

    @Entity(name = "Post")
    public static class Post {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private String name;

        @OneToOne(mappedBy = "post",
                cascade = CascadeType.ALL, orphanRemoval = true)
        private PostDetails details;

        public Long getId() {
            return id;
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
        private Long id;

        @Column(name = "created_on")
        @Temporal(TemporalType.TIMESTAMP)
        private Date createdOn = new Date();

        private boolean visible;

        @OneToOne
        @JoinColumn(name = "id")
        @MapsId
        private Post post;

        public Long getId() {
            return id;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public void setPost(Post post) {
            this.post = post;
        }
    }

    @Entity(name = "Commit")
    public static class Commit {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private String comment;

        @OneToOne(cascade = CascadeType.ALL)
        @JoinTable(name = "Branch_Merge_Commit",
                joinColumns = @JoinColumn(name = "commit_id", referencedColumnName = "id"),
                inverseJoinColumns = @JoinColumn(name = "branch_merge_id", referencedColumnName = "id")
        )
        private BranchMerge branchMerge;

        public Commit() {
        }

        public Commit(String comment) {
            this.comment = comment;
        }

        public Long getId() {
            return id;
        }

        public void addBranchMerge(String fromBranch, String toBranch) {
            this.branchMerge = new BranchMerge(fromBranch, toBranch);
        }

        public void removeBranchMerge() {
            this.branchMerge = null;
        }
    }

    @Entity(name = "BranchMerge")
    @Immutable
    public static class BranchMerge {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private String fromBranch;

        private String toBranch;

        public BranchMerge() {
        }

        public BranchMerge(String fromBranch, String toBranch) {
            this.fromBranch = fromBranch;
            this.toBranch = toBranch;
        }

        public Long getId() {
            return id;
        }
    }
}
