package com.vladmihalcea.hibernate.masterclass.laboratory.idgenerator;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.Session;
import org.junit.Test;

public class HiloIdentifierTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
                Hilo.class
        };
    }

    @Test
    public void testHiloIdentifierGenerator() {
        doInTransaction(new TransactionCallable<Void>() {
            @Override
            public Void execute(Session session) {

                for(int i = 0; i < 8; i++) {
                    Hilo hilo = new Hilo();
                    session.persist(hilo);
                    session.flush();
                }
                return null;
            }
        });
    }
}
