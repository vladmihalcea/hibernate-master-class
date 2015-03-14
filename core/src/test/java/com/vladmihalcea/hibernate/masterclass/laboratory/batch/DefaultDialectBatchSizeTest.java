package com.vladmihalcea.hibernate.masterclass.laboratory.batch;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractIntegrationTest;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.dialect.Dialect;
import org.junit.Test;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * DefaultDialectBatchSizeTest - Test to check the default dialect hibernate.jdbc.batch_size
 *
 * @author Vlad Mihalcea
 */
public class DefaultDialectBatchSizeTest extends BatchSizeTest {

    @Override
    protected Properties getProperties() {
        Properties properties = super.getProperties();
        properties.put("hibernate.jdbc.batch_size", String.valueOf(batchSize()));
        return properties;
    }
}
