package com.vladmihalcea.hibernate.masterclass.laboratory.flushing;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * User - User
 *
 * @author Vlad Mihalcea
 */
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(generator = "uuid2")
    private String id;

    private String favoriteColor;

    public User() {
    }

    public String getId() {
        return id;
    }

    public String getFavoriteColor() {
        return favoriteColor;
    }

    public void setFavoriteColor(String favoriteColor) {
        this.favoriteColor = favoriteColor;
    }
}
