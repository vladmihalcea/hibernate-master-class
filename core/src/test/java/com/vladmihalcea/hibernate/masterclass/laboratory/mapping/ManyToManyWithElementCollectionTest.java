package com.vladmihalcea.hibernate.masterclass.laboratory.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.junit.Ignore;
import org.junit.Test;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;

import static org.junit.Assert.assertEquals;

/**
 * ManyToManyWithElementCollectionTest - ManyToMany with ElementCollection Test
 *
 * @author Vlad Mihalcea
 */
public class ManyToManyWithElementCollectionTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[] {
            Clubber.class,
            Club.class
        };
    }

    @Test @Ignore
    public void testAddingEmbeddable() {
        final Clubber clubberReference = doInTransaction(session -> {
            Clubber clubber = new Clubber();
            Club club = new Club();
            clubber.addClub(club);
            session.persist(club);
            session.flush();
            return clubber;
        });

        doInTransaction(session -> {
            Clubber clubber = (Clubber) session.get(Clubber.class, clubberReference.getId());
            assertEquals(1, clubber.getClubs().size());
            assertEquals(1, clubber.getJoinDate().size());
        });
    }

    @Entity(name = "Clubber")
    public static class Clubber{

        @Id
        @GeneratedValue
        @Column(name = "Clubber_Id")
        private Integer id;

        @Temporal(TemporalType.TIMESTAMP)
        @ElementCollection
        @CollectionTable(name="CLUB_ASSIGNMENTS", joinColumns=@JoinColumn(name="Clubber_Id", referencedColumnName="Clubber_Id"))
        @Column(name="CLUB_DATE")
        @MapKeyJoinColumn(name = "Club_ID", referencedColumnName="Club_ID")
        private Map<Club, Date> joinDate = new HashMap<>();

        public Integer getId() {
            return id;
        }

        public Map<Club, Date> getJoinDate() {
            return joinDate;
        }

        public Collection<Club> getClubs() {
            return joinDate.keySet();
        }

        public void addClub(Club club) {
            joinDate.put(club, new Date());
            //clubs.add(club);
            club.getClubbers().add(this);
        }
    }

    @Entity(name = "Club")
    public static class Club {

        @Id
        @GeneratedValue
        @Column(name = "Club_ID")
        private Integer id;

        @ManyToMany(mappedBy = "joinDate", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
        private List<Clubber> clubbers = new ArrayList<>();

        public Integer getId() {
            return id;
        }

        public List<Clubber> getClubbers() {
            return clubbers;
        }
    }
}
