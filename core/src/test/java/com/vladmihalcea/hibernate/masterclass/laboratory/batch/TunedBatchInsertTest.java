package com.vladmihalcea.hibernate.masterclass.laboratory.batch;

import java.util.Properties;

/**
 * TunedBatchSizeTest - Test to check hibernate.jdbc.batch_size
 *
 * @author Vlad Mihalcea
 */
public class TunedBatchInsertTest extends DefaultDialectOrderedVersionedBatchingTest {

    protected int batchSize() {
        return 50;
    }
}
