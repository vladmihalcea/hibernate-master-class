package com.vladmihalcea.hibernate.masterclass.laboratory.flushing;

import javax.persistence.Transient;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * SelfDirtyCheckingEntity - Manual dirty checking mechanism
 *
 * @author Vlad Mihalcea
 */
public abstract class SelfDirtyCheckingEntity implements DirtyAware {

    private final Map<String, String> setterToPropertyMap = new HashMap<String, String>();

    @Transient
    private Set<String> dirtyProperties = new LinkedHashSet<String>();

    public SelfDirtyCheckingEntity() {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(getClass());
            PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor descriptor : descriptors) {
                Method setter = descriptor.getWriteMethod();
                if (setter != null) {
                    setterToPropertyMap.put(setter.getName(), descriptor.getName());
                }
            }
        } catch (IntrospectionException e) {
            throw new IllegalStateException(e);
        }

    }

    @Override
    public Set<String> getDirtyProperties() {
        return dirtyProperties;
    }

    @Override
    public void clearDirtyProperties() {
        dirtyProperties.clear();
    }

    protected void markDirtyProperty() {
        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        dirtyProperties.add(setterToPropertyMap.get(methodName));
    }
}
