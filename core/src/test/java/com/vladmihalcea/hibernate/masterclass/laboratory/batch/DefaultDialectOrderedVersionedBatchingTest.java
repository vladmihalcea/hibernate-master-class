package com.vladmihalcea.hibernate.masterclass.laboratory.batch;

import java.util.Properties;

/**
 * DefaultDialectOrderedVersionedBatchingTest - Test to check ordered inserts and updates and data versioned batching
 *
 * @author Vlad Mihalcea
 */
public class DefaultDialectOrderedVersionedBatchingTest extends DefaultDialectOrderedBatchingTest {

    @Override
    protected Properties getProperties() {
        Properties properties = super.getProperties();
        properties.put("hibernate.jdbc.batch_versioned_data", "true");
        return properties;
    }
}
