package com.vladmihalcea.hibernate.masterclass.laboratory.flushing;

import javax.persistence.*;

/**
 * Product - Product
 *
 * @author Vlad Mihalcea
 */
@Entity
@Table(name = "product")
@NamedNativeQueries(
        @NamedNativeQuery(name = "product_ids", query = "select id from product")
)
public class Product {

    @Id
    @GeneratedValue(generator = "uuid2")
    private String id;

    private String color;

    public Product() {
    }

    public String getId() {
        return id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
