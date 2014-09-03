package com.vladmihalcea.hibernate.masterclass.laboratory.idgenerator;

import java.util.Properties;

public class EnhancedSequenceVsTableGeneratorTest extends SequenceVsTableGeneratorTest {

    @Override
    protected Properties getProperties() {
        Properties properties = super.getProperties();
        properties.put("hibernate.id.new_generator_mappings", "true");
        return properties;
    }
}
