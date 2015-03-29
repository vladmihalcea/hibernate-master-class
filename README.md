hibernate-master-class
======================

[Hibernate Master Class](http://vladmihalcea.com/tutorials/hibernate/) is an advance course of one of the most popular JPA implementation

All examples require at least Java 1.8.0_31 or later.

The Unit Tests are run against HSQLDB, so no preliminary set-ups are required.

The Integration Tests require some external configurations:

* PostgreSQL
 
 You should install [PostgreSQL](http://www.postgresql.org/download/) 9.3 (or later) and then

 1. create a *postgres* user with the *admin* password
 2. create a *hibernate-master-class* database

* Oracle
