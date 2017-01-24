package com.vladmihalcea.hibernate.masterclass.laboratory.idgenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.junit.Test;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractMySQLIntegrationTest;

public class AutoIdentifierMySQLTest extends AbstractMySQLIntegrationTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
                Post.class,
        };
    }

    @Override
    protected boolean nativeHibernateSessionFactoryBootstrap() {
        return false;
    }

    @Test
    public void test() {
        doInJPA(entityManager -> {
            for ( int i = 1; i <= 3; i++ ) {
                entityManager.persist(
                    new Post(
                        String.format(
                            "High-Performance Java Persistence, Part %d", i
                        )
                    )
                );
            }
        });
    }

    @Entity(name = "Post")
    @Table(name = "post")
    public static class Post {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        private String title;

        public Post() {}

        public Post(String title) {
            this.title = title;
        }
    }
}
