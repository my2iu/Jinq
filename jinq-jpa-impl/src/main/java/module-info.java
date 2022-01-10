module org.jinq.jpa.impl {
   requires org.jinq.analysis;
   requires org.jinq.asmrebased;
   requires transitive org.jinq.api;
   requires jakarta.persistence;
   requires java.sql;
   exports org.jinq.jpa.jpqlquery;
   exports org.jinq.jpa.transform;
}