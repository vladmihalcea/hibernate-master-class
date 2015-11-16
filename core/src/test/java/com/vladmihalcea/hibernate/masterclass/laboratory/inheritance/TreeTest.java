package com.vladmihalcea.hibernate.masterclass.laboratory.inheritance;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.Session;
import org.junit.Test;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * TreeTest - Tree Test
 *
 * @author Vlad Mihalcea
 */
public class TreeTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
                LocalFolder.class,
                RemoteFolder.class,
        };
    }

    @Test
    public void testTree() {
        LOGGER.debug("testAddWebResource");
        doInTransaction(session -> {
                LocalFolder rootLocalFolder = new LocalFolder();
                session.persist(rootLocalFolder);
                LocalFolder localFolder1 = new LocalFolder();
                rootLocalFolder.addChild(localFolder1);
                session.persist(localFolder1);
                LocalFolder localFolder11 = new LocalFolder();
                localFolder1.addChild(localFolder11);
                session.persist(localFolder11);

                RemoteFolder rootRemoteFolder = new RemoteFolder();
                session.persist(rootRemoteFolder);
                RemoteFolder remoteFolder1 = new RemoteFolder();
                rootRemoteFolder.addChild(remoteFolder1);
                session.persist(remoteFolder1);
                RemoteFolder remoteFolder11 = new RemoteFolder();
                remoteFolder1.addChild(remoteFolder11);
                session.persist(remoteFolder11);
                return null;

        });
    }

    @MappedSuperclass
    public abstract class GenericHierarchicalDictionary {

        public abstract GenericHierarchicalDictionary getParent();

        public abstract Set<? extends GenericHierarchicalDictionary> getChildren();

    }

    @Entity
    @Table(name = "LocalFolder")
    public class LocalFolder extends GenericHierarchicalDictionary {

        @Id
        @GeneratedValue(strategy=GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        private LocalFolder parent;

        @OneToMany(mappedBy = "parent")
        private Set<LocalFolder> children = new HashSet<LocalFolder>();

        @Override
        public LocalFolder getParent() {
            return parent;
        }

        @Override
        public Set<LocalFolder> getChildren() {
            return children;
        }

        public void addChild(LocalFolder localFolder) {
            localFolder.parent = this;
            children.add(localFolder);
        }
    }

    @Entity
    @Table(name = "RemoteFolder")
    public class RemoteFolder extends GenericHierarchicalDictionary {

        @Id
        @GeneratedValue(strategy=GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        private RemoteFolder parent;

        @OneToMany(mappedBy = "parent")
        private Set<RemoteFolder> children = new HashSet<RemoteFolder>();

        @Override
        public RemoteFolder getParent() {
            return parent;
        }

        @Override
        public Set<RemoteFolder> getChildren() {
            return children;
        }

        public void addChild(RemoteFolder localFolder) {
            localFolder.parent = this;
            children.add(localFolder);
        }
    }
}
