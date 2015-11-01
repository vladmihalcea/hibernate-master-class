package com.vladmihalcea.hibernate.masterclass.laboratory.type;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractPostgreSQLIntegrationTest;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * <code>PostgreQuoteStringTest</code> - PostgreQuoteStringTest
 *
 * @author Vlad Mihalcea
 */
public class PostgreQuoteStringTest extends AbstractPostgreSQLIntegrationTest {
    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
                WidgetContentTo.class
        };
    }

    @Before
    public void init() {
        super.init();
        doInTransaction(session -> {
            WidgetContentTo widgetContentTo = new WidgetContentTo();
            widgetContentTo.setWidgetContentId(1);
            widgetContentTo.setSiteId(1);
            widgetContentTo.setWidgetContentName("aaa`bbb");
            session.persist(widgetContentTo);
        });
    }

    @Test
    public void test() {
        doInTransaction(session -> {
            List<WidgetContentTo> widgetContentTos = session.createQuery("from WidgetContentTo").list();
            assertEquals(1, widgetContentTos.size());
        });
    }

    @Entity(name = "WidgetContentTo")
    public static class WidgetContentTo
    {
        @Id
        private int         widgetContentId;
        private int         siteId;
        private String      widgetContentName;

        public int getWidgetContentId() {
            return widgetContentId;
        }

        public void setWidgetContentId(int widgetContentId) {
            this.widgetContentId = widgetContentId;
        }

        public int getSiteId() {
            return siteId;
        }

        public void setSiteId(int siteId) {
            this.siteId = siteId;
        }

        public String getWidgetContentName() {
            return widgetContentName;
        }

        public void setWidgetContentName(String widgetContentName) {
            this.widgetContentName = widgetContentName;
        }
    }
}
