package com.vladmihalcea.hibernate.masterclass.laboratory.flushing;

import org.hibernate.EmptyInterceptor;
import org.hibernate.Interceptor;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * InterceptorDirtyCheckingTest - Test to check interceptor dirty checking capabilities
 *
 * @author Vlad Mihalcea
 */
public class InterceptorDirtyCheckingTest extends AutoDirtyCheckingTest {

    public class DirtyCheckingInterceptor extends EmptyInterceptor {
        @Override
        public int[] findDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
            if(entity instanceof DirtyAware) {
                DirtyAware dirtyAware = (DirtyAware) entity;
                Set<String> dirtyProperties = dirtyAware.getDirtyProperties();
                int[] dirtyPropertiesIndices = new int[dirtyProperties.size()];
                List<String> propertyNamesList = Arrays.asList(propertyNames);
                int i = 0;
                for(String dirtyProperty : dirtyProperties) {
                    LOGGER.info("The {} property is dirty", dirtyProperty);
                    dirtyPropertiesIndices[i++] = propertyNamesList.indexOf(dirtyProperty);
                }
                dirtyAware.clearDirtyProperties();
                return dirtyPropertiesIndices;
            }
            return super.findDirty(entity, id, currentState, previousState, propertyNames, types);
        }
    }

    @Override
    protected Interceptor interceptor() {
        return new DirtyCheckingInterceptor();
    }
}
