package com.vladmihalcea.hibernate.masterclass.laboratory.entityidentifier;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * EntityEvent - Entity Event
 *
 * @author Vlad Mihalcea
 */
@Entity
@Table(name = "entity_event")
public class EntityEvent {

    @Id
    @GeneratedValue
    private Long id;

    private String message;

    private EntityIdentifier entityIdentifier;

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public EntityIdentifier getEntityIdentifier() {
        return entityIdentifier;
    }

    public void setEntityIdentifier(EntityIdentifier entityIdentifier) {
        this.entityIdentifier = entityIdentifier;
    }
}
