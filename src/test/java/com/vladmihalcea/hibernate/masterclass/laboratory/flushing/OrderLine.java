package com.vladmihalcea.hibernate.masterclass.laboratory.flushing;

import javax.persistence.*;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Order - Order
 *
 * @author Vlad Mihalcea
 */
@Entity
@Table(name = "ORDER_LINE")
public class OrderLine implements DirtyAware {

    public static final Map<String, String> SETTER_TO_PROPERTY_MAP = new HashMap<String, String>();

    static {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(OrderLine.class);
            PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor descriptor : descriptors) {
                Method setter = descriptor.getWriteMethod();
                if (setter != null) {
                    SETTER_TO_PROPERTY_MAP.put(setter.getName(), descriptor.getName());
                }
            }
        } catch (IntrospectionException e) {
            throw new IllegalStateException(e);
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long number;

    private String orderedBy;

    private Date orderedOn;

    @Transient
    private Set<String> dirtyProperties = new LinkedHashSet<String>();

    public OrderLine() {

    }

    public Long getId() {
        return id;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
        markDirtyProperty();
    }

    public String getOrderedBy() {
        return orderedBy;
    }

    public void setOrderedBy(String orderedBy) {
        this.orderedBy = orderedBy;
        markDirtyProperty();
    }

    public Date getOrderedOn() {
        return orderedOn;
    }

    public void setOrderedOn(Date orderedOn) {
        this.orderedOn = orderedOn;
        markDirtyProperty();

    }

    @Override
    public Set<String> getDirtyProperties() {
        return dirtyProperties;
    }

    @Override
    public void clearDirtyProperties() {
        dirtyProperties.clear();
    }

    private void markDirtyProperty() {
        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        dirtyProperties.add(SETTER_TO_PROPERTY_MAP.get(methodName));
    }
}
