package com.vladmihalcea.hibernate.masterclass.laboratory.entityidentifier;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * EntityAttribute - Entity Attribute
 *
 * @author Vlad Mihalcea
 */
@Entity
@Table(name = "entity_attribute")
public class EntityAttribute {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String value;

    private EntityIdentifier entityIdentifier;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public EntityIdentifier getEntityIdentifier() {
        return entityIdentifier;
    }

    public void setEntityIdentifier(EntityIdentifier entityIdentifier) {
        this.entityIdentifier = entityIdentifier;
    }
}
