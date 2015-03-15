package com.vladmihalcea.hibernate.masterclass.laboratory.batch;

import java.util.Properties;

/**
 * DefaultDialectOrderedBatchingTest - Test to check ordered inserts and updates batching
 *
 * @author Vlad Mihalcea
 */
public class DefaultDialectOrderedBatchingTest extends DefaultDialectBatchingTest {

    @Override
    protected Properties getProperties() {
        Properties properties = super.getProperties();
        properties.put("hibernate.order_inserts", "true");
        properties.put("hibernate.order_updates", "true");
        return properties;
    }
}
