package com.vladmihalcea.hibernate.masterclass.laboratory.testenv;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
* SecurityId - SecurityId
*
* @author Vlad Mihalcea
*/
@Entity
class SecurityId {
    @Id
    @GeneratedValue
    private Long id;

    private String role;

    public Long getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
