package com.vladmihalcea.hibernate.masterclass.laboratory.entityidentifier;

import javax.persistence.*;

/**
 * Product - Product
 *
 * @author Vlad Mihalcea
 */
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    public Product() {
    }

    public Product(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
