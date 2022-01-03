/**
 * If you want to build a modular version of the sample code, include
 * this module-info.java in the build and exclude the top-level
 * SampleMain.java
 */
module com.example.jinq.sample {
   requires jakarta.persistence;
   requires java.sql;
   requires org.jinq.jpa;
   exports com.example.jinq.sample;
   exports com.example.jinq.sample.jpa.entities;
   opens com.example.jinq.sample.jpa.entities;
}