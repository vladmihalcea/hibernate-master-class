Hibernate Master Class Tutorial
======================

[Hibernate Master Class](http://vladmihalcea.com/tutorials/hibernate/) is an advance course of one of the most popular JPA implementation

All examples require at least Java 1.8.0_31 or later.

The Unit Tests are run against HSQLDB, so no preliminary set-ups are required.

The Integration Tests require some external configurations:

* PostgreSQL
 
 You should install [PostgreSQL](http://www.postgresql.org/download/) 9.4 (or later) and the password for the *postgres* user should be *admin*

 Now you need to create a *hibernate-master-class* database

* Oracle
 
 You need to download and install [Oracle XE](http://www.oracle.com/technetwork/database/database-technologies/express-edition/overview/index.html) 

 Set the *sys* pasword to *admin*

 You need to download the [Orcale JDBC Driver (ojdbc6.jar)](http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-112010-090769.html), which is not available in the Maven Central Repository.
 
 You need to install the ojdbc6.jar on your local Maven repository using the following command:
 
 $ mvn install:install-file -Dfile=ojdbc6.jar -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.2.0.4 -Dpackaging=jar
 
* MySQL
  
 You should install [MySQL](http://dev.mysql.com/downloads/) 5.6 (or later) and the password for the *mysql* user should be *admin*
 
 Now you need to create a *hibernate-master-class* schema
