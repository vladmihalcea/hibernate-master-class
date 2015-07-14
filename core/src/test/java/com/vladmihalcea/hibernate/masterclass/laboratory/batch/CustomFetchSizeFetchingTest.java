package com.vladmihalcea.hibernate.masterclass.laboratory.batch;

import java.util.Properties;

/**
 * CustomFetchSizeFetchingTest - Test to check custom fetch_size
 *
 * @author Vlad Mihalcea
 */
public class CustomFetchSizeFetchingTest extends NoFetchingTest {

    @Override
    protected Properties getProperties() {
        Properties properties = super.getProperties();
        properties.put("hibernate.jdbc.fetch_size", fetchSize());
        return properties;
    }

    protected int fetchSize() {
        return 10;
    }
}
