package com.vladmihalcea.hibernate.masterclass.laboratory.cascade;

import com.vladmihalcea.hibernate.masterclass.laboratory.util.AbstractTest;
import org.hibernate.Session;
import org.junit.Test;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


/**
 * OneToOneCascadeTest - Test to check @OneToOne Cascading
 *
 * @author Vlad Mihalcea
 */
public class ManyToManyCascadeDissociationTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
                Book.class,
                Author.class
        };
    }

    public void addBooks() {
        doInTransaction(session -> {
            Author _John_Smith = new Author("John Smith");
            Author _Michelle_Diangello = new Author("Michelle Diangello");
            Author _Mark_Armstrong = new Author("Mark Armstrong");

            Book _Day_Dreaming = new Book("Day Dreaming");
            Book _Day_Dreaming_2nd = new Book("Day Dreaming, Second Edition");

            _John_Smith.addBook(_Day_Dreaming);
            _Michelle_Diangello.addBook(_Day_Dreaming);

            _John_Smith.addBook(_Day_Dreaming_2nd);
            _Michelle_Diangello.addBook(_Day_Dreaming_2nd);
            _Mark_Armstrong.addBook(_Day_Dreaming_2nd);

            session.persist(_John_Smith);
            session.persist(_Michelle_Diangello);
            session.persist(_Mark_Armstrong);
        });
    }

    @Test
    public void testCascadeTypeDelete() {
        LOGGER.info("Test CascadeType.DELETE");

        addBooks();

        doInTransaction(session -> {
            Author _Mark_Armstrong = getByName(session, "Mark Armstrong");
            _Mark_Armstrong.remove();
            session.delete(_Mark_Armstrong);
            session.flush();
            Author _John_Smith = getByName(session, "John Smith");
            assertEquals(2, _John_Smith.books.size());
        });

    }

    private Author getByName(Session session, String fullName) {
        return (Author) session
                .createQuery("select a from Author a join fetch a.books where a.fullName = :fullName")
                .setParameter("fullName", fullName)
                .uniqueResult();
    }

    @Entity(name = "Author")
    public static class Author {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;

        @Column(name = "full_name", nullable = false)
        private String fullName;

        @ManyToMany(mappedBy = "authors", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
        private List<Book> books = new ArrayList<>();

        private Author() {}

        public Author(String fullName) {
            this.fullName = fullName;
        }

        public Long getId() {
            return id;
        }

        public void addBook(Book book) {
            books.add(book);
            book.authors.add(this);
        }

        public void removeBook(Book book) {
            books.remove(book);
            book.authors.remove(this);
        }

        public void remove() {
            for(Book book : new ArrayList<>(books)) {
                removeBook(book);
            }
        }
    }

    @Entity(name = "Book")
    public static class Book {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;

        @Column(name = "title", nullable = false)
        private String title;

        @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
        @JoinTable(name = "Book_Author",
            joinColumns = {@JoinColumn(name = "book_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "author_id", referencedColumnName = "id")}
        )
        private List<Author> authors = new ArrayList<>();

        private Book() {}

        public Book(String title) {
            this.title = title;
        }
    }



}
