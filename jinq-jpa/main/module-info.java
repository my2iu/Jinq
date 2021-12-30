module org.jinq.jpa {
   requires org.jinq.analysis;
   requires org.jinq.asmrebased;
   requires transitive org.jinq.api;
   requires jakarta.persistence;
   requires java.sql;
   exports org.jinq.jpa;
   exports org.jinq.jpa.jpqlquery to org.jinq.hibernate;
   exports org.jinq.jpa.transform to org.jinq.hibernate;
}