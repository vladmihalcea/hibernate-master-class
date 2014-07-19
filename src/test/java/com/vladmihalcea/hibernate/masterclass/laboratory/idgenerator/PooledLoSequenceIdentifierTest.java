package com.vladmihalcea.hibernate.masterclass.laboratory.idgenerator;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

public class PooledLoSequenceIdentifierTest extends PooledSequenceIdentifierTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
                CollisionFreeSequenceIdentifier.class
        };
    }

    @Override
    protected Object newEntityInstance() {
        return new CollisionFreeSequenceIdentifier();
    }

    @Entity(name = "sequenceIdentifier")
     public static class CollisionFreeSequenceIdentifier {

        @Id
        @GenericGenerator(name = "sampleGenerator", strategy = "enhanced-sequence",
                parameters = {
                        @org.hibernate.annotations.Parameter(name = "optimizer",
                                value = "pooled-lo"
                        ),
                        @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                        @org.hibernate.annotations.Parameter(name = "increment_size", value = "5")
                }
        )
        @GeneratedValue(strategy = GenerationType.TABLE, generator = "sampleGenerator")
        private Long id;
    }
}
