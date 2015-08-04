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

 Set the *sys* password to *admin*
 
 Connect to Oracle using the "sys as sysdba" user and create a new user:
 
 > create user oracle identified by admin default tablespace users;
 >
 > grant dba to oracle;
 > 
 > alter system set processes=1000 scope=spfile;
 >
 > alter system set sessions=1000 scope=spfile;

 You need to download the [Orcale JDBC Driver (ojdbc6.jar)](http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-112010-090769.html), which is not available in the Maven Central Repository.
 
 You need to install the ojdbc6.jar on your local Maven repository using the following command:
 
 > $ mvn install:install-file -Dfile=ojdbc6.jar -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.2.0.4 -Dpackaging=jar
 
* MySQL
  
 You should install [MySQL](http://dev.mysql.com/downloads/) 5.6 (or later) and the password for the *mysql* user should be *admin*
 
 Now you need to create a *hibernate-master-class* schema

* SQL Server

 You should install [SQL Server Express Edition with Tools](http://www.microsoft.com/en-us/server-cloud/products/sql-server-editions/sql-server-express.aspx)
 Chose mixed mode authentication and set the *sa* user password to *adm1n*

 Open SQL Server Management Studio and create the *hibernate_master_class* database
 
 Open SQL Server Configuration Manager -> SQL Server Network Configuration and [enable Named Pipes and TCP](http://stackoverflow.com/questions/18841744/jdbc-connection-failed-error-tcp-ip-connection-to-host-failed)

 You need to [download the SQL Server JDBC Driver](https://www.microsoft.com/en-us/download/details.aspx?displaylang=en&id=11774) and install the *sqljdbc4.jar* on your local Maven repository using the following command:
 
 > $ mvn install:install-file -Dfile=sqljdbc4.jar -Dpackaging=jar -DgroupId=com.microsoft.sqlserver -DartifactId=sqljdbc4 -Dversion=4.0