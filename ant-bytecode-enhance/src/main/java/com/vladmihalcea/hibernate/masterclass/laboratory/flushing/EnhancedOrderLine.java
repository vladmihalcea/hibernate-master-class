package com.vladmihalcea.hibernate.masterclass.laboratory.flushing;

import javax.persistence.*;
import java.util.Date;

/**
 * Order - Order
 *
 * @author Vlad Mihalcea
 */
@Entity
public class EnhancedOrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long number;

    private String orderedBy;

    private Date orderedOn;

    public Long getId() {
        return id;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public String getOrderedBy() {
        return orderedBy;
    }

    public void setOrderedBy(String orderedBy) {
        this.orderedBy = orderedBy;
    }

    public Date getOrderedOn() {
        return orderedOn;
    }

    public void setOrderedOn(Date orderedOn) {
        this.orderedOn = orderedOn;
    }
}
