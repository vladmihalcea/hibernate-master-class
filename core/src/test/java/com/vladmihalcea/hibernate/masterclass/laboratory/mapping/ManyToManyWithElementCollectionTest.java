package com.vladmihalcea.hibernate.masterclass.laboratory.mapping;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.junit.Test;

import javax.persistence.*;
import java.util.*;

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

    @Test
    public void testAddingEmbeddable() {
        doInTransaction(session -> {
            Clubber clubber = new Clubber();
            Club club = new Club();
            clubber.addClub(club);
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

        @ManyToMany(cascade = CascadeType.PERSIST)
        @JoinTable(name = "CLUB_ASSIGNMENTS",
                joinColumns = @JoinColumn(name="Clubber_Id", referencedColumnName="Clubber_Id"),
                inverseJoinColumns = @JoinColumn(name="Club_ID", referencedColumnName="Club_ID"))
        private List<Club> clubs = new ArrayList<>();


        public Integer getId() {
            return id;
        }

        public Map<Club, Date> getJoinDate() {
            return joinDate;
        }

        public List<Club> getClubs() {
            return clubs;
        }

        public void addClub(Club club) {
            joinDate.put(club, new Date());
            clubs.add(club);
            club.getClubbers().add(this);
        }
    }

    @Entity(name = "Club")
    public static class Club {

        @Id
        @GeneratedValue
        @Column(name = "Club_ID")
        private Integer id;

        @ManyToMany(cascade = CascadeType.PERSIST, mappedBy = "clubs")
        private List<Clubber> clubbers = new ArrayList<>();

        public Integer getId() {
            return id;
        }

        public List<Clubber> getClubbers() {
            return clubbers;
        }
    }
}
