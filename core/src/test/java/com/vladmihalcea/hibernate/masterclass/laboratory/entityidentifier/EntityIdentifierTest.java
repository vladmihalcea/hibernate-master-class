package com.vladmihalcea.hibernate.masterclass.laboratory.entityidentifier;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class EntityIdentifierTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
                Product.class,
                EntityEvent.class,
                EntityAttribute.class,
        };
    }

    @Test
    public void testEntityIdentifier() {
        doInTransaction(session -> {
            Product product = new Product("LCD");
            session.persist(product);
            EntityEvent productEvent = new EntityEvent();
            productEvent.setMessage(String.format(
                "Product %s added", product.getName()));
            productEvent.setEntityIdentifier(
                new EntityIdentifier(
                    product.getClass(),
                    product.getId()
            ));
            session.persist(productEvent);
            EntityAttribute productAttribute =
                new EntityAttribute();
            productAttribute.setName("AD_CAMPAIGN");
            productAttribute.setValue("LCD_Sales");
            productAttribute.setEntityIdentifier(
                new EntityIdentifier(
                    product.getClass(),
                    product.getId()
            ));
            session.persist(productAttribute);
            assertSame(1, session.createQuery(
                "select ea " +
                "from EntityAttribute ea " +
                "where " +
                "   ea.entityIdentifier = :entityIdentifier")
                .setParameter("entityIdentifier",
                    new EntityIdentifier(
                        product.getClass(), product.getId()))
                .list().size());
            return null;
        });
    }
}
