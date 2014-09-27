package com.vladmihalcea.hibernate.masterclass.laboratory.concurrency;

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
    private Long id;

    private long quantity;

    private int likes;

    public Product() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public int getLikes() {
        return likes;
    }

    public int incrementLikes() {
        return ++likes;
    }
}
