package com.vladmihalcea.hibernate.masterclass.laboratory.flushing;

import org.hibernate.CustomEntityDirtinessStrategy;
import org.hibernate.Session;
import org.hibernate.persister.entity.EntityPersister;

import java.util.Properties;

/**
 * InterceptorDirtyCheckingTest - Test to check CustomEntityDirtinessStrategy dirty checking capabilities
 *
 * @author Vlad Mihalcea
 */
public class CustomEntityDirtinessStrategyTest extends AutoDirtyCheckingTest {

    public static class EntityDirtinessStrategy implements CustomEntityDirtinessStrategy {

        @Override
        public boolean canDirtyCheck(Object entity, EntityPersister persister, Session session) {
            return entity instanceof DirtyAware;
        }

        @Override
        public boolean isDirty(Object entity, EntityPersister persister, Session session) {
            return !cast(entity).getDirtyProperties().isEmpty();
        }

        @Override
        public void resetDirty(Object entity, EntityPersister persister, Session session) {
            cast(entity).clearDirtyProperties();
        }

        @Override
        public void findDirty(Object entity, EntityPersister persister, Session session, DirtyCheckContext dirtyCheckContext) {
            final DirtyAware dirtyAware = cast(entity);
            dirtyCheckContext.doDirtyChecking(
                    new AttributeChecker() {
                        @Override
                        public boolean isDirty(AttributeInformation attributeInformation) {
                            return dirtyAware.getDirtyProperties().contains( attributeInformation.getName() );
                        }
                    }
            );
        }

        private DirtyAware cast(Object entity) {
            return DirtyAware.class.cast(entity);
        }
    }

    @Override
    protected Properties getProperties() {
        Properties properties = super.getProperties();
        properties.setProperty("hibernate.entity_dirtiness_strategy", EntityDirtinessStrategy.class.getName());
        return properties;
    }
}
