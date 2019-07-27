Hibernate Master Class Tutorial
======================

[Hibernate Master Class](http://vladmihalcea.com/tutorials/hibernate/) is an advance course of one of the most popular JPA implementation

All examples require at least Java 1.8.0 or later.

The Unit Tests are run against HSQLDB, so no preliminary set-ups are required.

### Are you struggling with application performance issues?

<a href="https://vladmihalcea.com/hypersistence-optimizer/?utm_source=GitHub&utm_medium=banner&utm_campaign=hibernatemasterclass">
<img src="https://vladmihalcea.com/wp-content/uploads/2019/03/Hypersistence-Optimizer-300x250.jpg" alt="Hypersistence Optimizer">
</a>

Imagine having a tool that can automatically detect if you are using JPA and Hibernate properly. No more performance issues, no more having to spend countless hours trying to figure out why your application is barely crawling.

Imagine discovering early during the development cycle that you are using suboptimal mappings and entity relationships or that you are missing performance-related settings. 

More, with Hypersistence Optimizer, you can detect all such issues during testing and make sure you don't deploy to production a change that will affect data access layer performance.

[Hypersistence Optimizer](https://vladmihalcea.com/hypersistence-optimizer/?utm_source=GitHub&utm_medium=banner&utm_campaign=hibernatemasterclass) is the tool you've been long waiting for!

#### Training

If you are interested in on-site training, I can offer you my [High-Performance Java Persistence training](https://vladmihalcea.com/trainings/?utm_source=GitHub&utm_medium=banner&utm_campaign=hibernatemasterclass)
which can be adapted to one, two or three days of sessions. For more details, check out [my website](https://vladmihalcea.com/trainings/?utm_source=GitHub&utm_medium=banner&utm_campaign=hibernatemasterclass).

#### Consulting

If you want me to review your application and provide insight into how you can optimize it to run faster, 
then check out my [consulting page](https://vladmihalcea.com/consulting/?utm_source=GitHub&utm_medium=banner&utm_campaign=hibernatemasterclass).

#### High-Performance Java Persistence Video Courses

If you want the fastest way to learn how to speed up a Java database application, then you should definitely enroll in [my High-Performance Java Persistence video courses](https://vladmihalcea.com/courses/?utm_source=GitHub&utm_medium=banner&utm_campaign=hibernatemasterclass).

#### High-Performance Java Persistence Book

Or, if you prefer reading books, you are going to love my [High-Performance Java Persistence book](https://vladmihalcea.com/books/high-performance-java-persistence?utm_source=GitHub&utm_medium=banner&utm_campaign=hibernatemasterclass) as well.

<a href="https://vladmihalcea.com/books/high-performance-java-persistence?utm_source=GitHub&utm_medium=banner&utm_campaign=hibernatemasterclass">
<img src="https://i0.wp.com/vladmihalcea.com/wp-content/uploads/2018/01/HPJP_h200.jpg" alt="High-Performance Java Persistence book">
</a>

<a href="https://vladmihalcea.com/courses?utm_source=GitHub&utm_medium=banner&utm_campaign=hibernatemasterclass">
<img src="https://i0.wp.com/vladmihalcea.com/wp-content/uploads/2018/01/HPJP_Video_Vertical_h200.jpg" alt="High-Performance Java Persistence video course">
</a>

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

 You need to download the [Oracle JDBC Driver (ojdbc6.jar or ojdbc7_g.jar)](http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-112010-090769.html), which is not available in the Maven Central Repository.
 
 You need to install the ojdbc6.jar or ojdbc7_g.jar on your local Maven repository using the following command:
 
 > $ mvn install:install-file -Dfile=ojdbc6.jar -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.2.0.4 -Dpackaging=jar
 
 > $ mvn install:install-file -Dfile=ojdbc7_g.jar -DgroupId=com.oracle -DartifactId=ojdbc7_g -Dversion=12.1.0.1 -Dpackaging=jar 
 
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
