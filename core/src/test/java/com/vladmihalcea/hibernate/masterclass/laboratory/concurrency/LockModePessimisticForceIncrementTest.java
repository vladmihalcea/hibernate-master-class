package com.vladmihalcea.hibernate.masterclass.laboratory.concurrency;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.StaleObjectStateException;
import org.hibernate.annotations.Immutable;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.fail;


/**
 * LockModePessimisticForceIncrementTest - Test to check LockMode.PESSIMISTIC_FORCE_INCREMENT
 *
 * @author Vlad Mihalcea
 */
public class LockModePessimisticForceIncrementTest extends AbstractTest {

    private final CountDownLatch startLatch = new CountDownLatch(1);
    private final CountDownLatch endLatch = new CountDownLatch(1);

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
                Repository.class,
                Commit.class
        };
    }

    @Before
    public void init() {
        super.init();
        doInTransaction(session -> {
            Repository repository = new Repository("Hibernate-Master-Class");
            session.persist(repository);
            session.flush();
        });
    }

    @Test
    public void testPessimisticForceIncrementLocking() throws InterruptedException {
        LOGGER.info("Test Single PESSIMISTIC_FORCE_INCREMENT Lock Mode ");
        doInTransaction(session -> {
            Repository repository = (Repository) session.get(Repository.class, 1L);
            session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_FORCE_INCREMENT)).lock(repository);
            Commit commit = new Commit(repository);
            commit.getChanges().add(new Change("README.txt", "0a1,5..."));
            commit.getChanges().add(new Change("web.xml", "17c17..."));
            session.persist(commit);
        });
    }

    @Test
    public void testConcurrentPessimisticForceIncrementLockingWithLockWaiting() throws InterruptedException {
        LOGGER.info("Test Concurrent PESSIMISTIC_FORCE_INCREMENT Lock Mode With Lock Waiting");
        doInTransaction(session -> {
            try {
                Repository repository = (Repository) session.get(Repository.class, 1L);
                session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_FORCE_INCREMENT)).lock(repository);

                executeAsync(() -> doInTransaction(_session -> {
                    LOGGER.info("Try to get the Repository row");
                    startLatch.countDown();
                    Repository _repository = (Repository) _session.get(Repository.class, 1L);
                    _session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_FORCE_INCREMENT)).lock(_repository);
                    Commit _commit = new Commit(_repository);
                    _commit.getChanges().add(new Change("index.html", "0a1,2..."));
                    _session.persist(_commit);
                    _session.flush();
                    endLatch.countDown();
                }));
                startLatch.await();
                LOGGER.info("Sleep for 500ms to delay the other transaction PESSIMISTIC_FORCE_INCREMENT Lock Mode acquisition");
                Thread.sleep(500);
                Commit commit = new Commit(repository);
                commit.getChanges().add(new Change("README.txt", "0a1,5..."));
                commit.getChanges().add(new Change("web.xml", "17c17..."));
                session.persist(commit);
            } catch (InterruptedException e) {
                fail("Unexpected failure");
            }
        });
        endLatch.await();
    }

    @Test
    public void testConcurrentPessimisticForceIncrementLockingFailFast() throws InterruptedException {
        LOGGER.info("Test Concurrent PESSIMISTIC_FORCE_INCREMENT Lock Mode fail fast");
        doInTransaction(session -> {
            try {
                Repository repository = (Repository) session.get(Repository.class, 1L);

                executeSync(() -> {
                    doInTransaction(_session -> {
                        Repository _repository = (Repository) _session.get(Repository.class, 1L);
                        _session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_FORCE_INCREMENT)).lock(_repository);
                        Commit _commit = new Commit(_repository);
                        _commit.getChanges().add(new Change("index.html", "0a1,2..."));
                        _session.persist(_commit);
                        _session.flush();
                    });
                });
                session.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_FORCE_INCREMENT)).lock(repository);
                fail("Should have thrown StaleObjectStateException!");
            } catch (StaleObjectStateException expected) {
                LOGGER.info("Failure: ", expected);
            }
        });
    }

    /**
     * Repository - Repository
     *
     * @author Vlad Mihalcea
     */
    @Entity(name = "repository")
    @Table(name = "repository")
    public static class Repository {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private String name;

        public Repository() {
        }

        public Repository(String name) {
            this.name = name;
        }

        @Version
        private int version;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

    /**
     * OrderLine - Order Line
     *
     * @author Vlad Mihalcea
     */
    @Entity(name = "Commit")
    @Table(name = "commit")
    @Immutable
    public static class Commit {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        @ManyToOne
        private Repository repository;

        @ElementCollection
        @CollectionTable(
                name = "commit_change",
                joinColumns = @JoinColumn(name = "commit_id")
        )
        private List<Change> changes = new ArrayList<>();

        public Commit() {
        }

        public Commit(Repository repository) {
            this.repository = repository;
        }

        public Repository getRepository() {
            return repository;
        }

        public List<Change> getChanges() {
            return changes;
        }
    }

    /**
     * OrderLine - Order Line
     *
     * @author Vlad Mihalcea
     */
    @Embeddable
    public static class Change {

        private String path;

        private String diff;

        public Change() {
        }

        public Change(String path, String diff) {
            this.path = path;
            this.diff = diff;
        }

        public String getPath() {
            return path;
        }

        public String getDiff() {
            return diff;
        }
    }
}
