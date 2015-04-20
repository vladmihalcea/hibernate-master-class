package com.vladmihalcea.hibernate.masterclass.laboratory.cache;

import java.util.Properties;


/**
 * ReadOnlyCacheConcurrencyStrategyTest - Test to check CacheConcurrencyStrategy.READ_ONLY
 *     with hibernate.cache.use_reference_entries doesn't work because Commit has a collection of CommitChanges
 *
 * @author Vlad Mihalcea
 */
public class ReadOnlyCacheConcurrencyStrategyReferenceEntitiesTest extends ReadOnlyCacheConcurrencyStrategyTest {

    @Override
    protected Properties getProperties() {
        Properties properties = super.getProperties();
        properties.put("hibernate.cache.use_second_level_cache", Boolean.TRUE.toString());
        properties.put("hibernate.cache.region.factory_class", "org.hibernate.cache.ehcache.EhCacheRegionFactory");
        properties.put("hibernate.cache.use_reference_entries", Boolean.TRUE.toString());
        return properties;
    }

}
