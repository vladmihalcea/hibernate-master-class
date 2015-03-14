package com.vladmihalcea.hibernate.masterclass.laboratory.batch;

import java.util.Properties;

/**
 * TunedBatchSizeTest - Test to check hibernate.jdbc.batch_size
 *
 * @author Vlad Mihalcea
 */
public class TunedBatchSizeTest extends BatchSizeTest {

    @Override
    protected Properties getProperties() {
        Properties properties = super.getProperties();
        properties.put("hibernate.jdbc.batch_size", String.valueOf(batchSize()));
        return properties;
    }

    protected int batchSize() {
        return 60;
    }
}
