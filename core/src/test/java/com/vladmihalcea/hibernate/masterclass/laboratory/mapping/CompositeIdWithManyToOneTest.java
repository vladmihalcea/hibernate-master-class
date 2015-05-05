package com.vladmihalcea.hibernate.masterclass.laboratory.mapping;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * CompositeIdWithManyToOneTest - CompositeIdWithManyToOneTest
 *
 * @author Vlad Mihalcea
 */
public class CompositeIdWithManyToOneTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
                Car.class,
                Factory.class,
                CarFactory.class
        };
    }

    @Before
    public void init() {
        super.init();
        doInTransaction(session -> {
            Car car = new Car();
            car.transmission = "Auto";

            Car car1 = new Car();
            car1.transmission = "Manual";

            Factory factory = new Factory();
            session.persist(factory);
            session.persist(car);
            session.persist(car1);

            CarFactory carFactory = new CarFactory();
            carFactory.setCar(car);
            carFactory.setFactory(factory);

            CarFactory carFactory1 = new CarFactory();
            carFactory1.setCar(car1);
            carFactory1.setFactory(factory);

            session.persist(carFactory);
            session.persist(carFactory1);
        });
    }

    @Test
    public void test() {
        doInTransaction(session -> {
            List<CarFactory> carFactoryList = session.createQuery("from CarFactory").list();
            assertEquals(2, carFactoryList.size());
        });
    }

    @Entity(name = "Car")
    public static class Car implements Serializable {

        @Id
        @GeneratedValue
        long id;

        @Column(name="transmission", nullable = false)
        String transmission;
        @OneToMany(fetch = FetchType.LAZY, mappedBy = "car")
        Set<CarFactory> factories;
    }

    @Entity(name = "Factory")
    public static class Factory  implements Serializable {

        @Id
        @GeneratedValue
        long id;
    }

    @Entity(name = "CarFactory")
    public static class CarFactory implements Serializable {

        @Id
        @ManyToOne
        @JoinColumn(name = "transmission", referencedColumnName = "transmission")
        Car car;

        @ManyToOne
        @Id
        Factory factory;

        public void setCar(Car car) {
            this.car = car;
        }

        public void setFactory(Factory factory) {
            this.factory = factory;
        }
    }
}
