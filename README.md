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
 
 You need to download [Oracle XE](http://www.oracle.com/technetwork/database/database-technologies/express-edition/overview/index.html)

 You need to download the [Orcale JDBC Driver (ojdbc6.jar)](http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-112010-090769.html), which is not available in the Maven Central Repository.
 
 You need to install the ojdbc6.jar on your local Maven repository using the following command:
 
 $ mvn install:install-file -Dfile=ojdbc6.jar -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.2.0.4 -Dpackaging=jar
