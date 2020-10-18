module org.jinq.jpa {
   requires org.jinq.analysis;
   requires transitive org.jinq.api;
   requires java.persistence;
   requires java.sql;
   exports org.jinq.jpa;
   exports org.jinq.jpa.jpqlquery to org.jinq.hibernate;
   exports org.jinq.jpa.transform to org.jinq.hibernate;
}