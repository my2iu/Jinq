module org.jinq.hibernate {
   exports org.jinq.hibernate;
   requires org.jinq.jpa;
   requires org.jinq.analysis;
   requires transitive org.jinq.api;
   requires java.persistence;
   requires java.sql;
   requires org.hibernate.orm.core;
   requires java.naming;
}