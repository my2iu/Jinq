module org.jinq.hibernate {
   exports org.jinq.hibernate;
   requires org.jinq.jpa;
   requires org.jinq.jpa.impl;
   requires org.jinq.analysis;
   requires org.jinq.asmrebased;
   requires transitive org.jinq.api;
   requires jakarta.persistence;
   requires java.sql;
   requires org.hibernate.orm.core;
   requires java.naming;
}