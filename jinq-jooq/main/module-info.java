module org.jinq.jooq {
   requires transitive org.jinq.api;
   requires org.jinq.analysis;
   requires org.jinq.asmrebased;
   requires org.jooq;
   requires java.base;
   exports org.jinq.jooq;
}