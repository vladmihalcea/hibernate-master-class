package com.vladmihalcea.hibernate.masterclass.laboratory.batch;

import java.util.Properties;

/**
 * DefaultDialectBatchingTest - Test to check the default dialect hibernate.jdbc.batch_size
 *
 * @author Vlad Mihalcea
 */
public class DefaultDialectBatchingTest extends NoBatchingTest {

    @Override
    protected Properties getProperties() {
        Properties properties = super.getProperties();
        properties.put("hibernate.jdbc.batch_size", String.valueOf(batchSize()));
        return properties;
    }
}
