/**
 * This is a module used for ant builds of Jinq when all of the Jinq
 * classes are compiled together into a single .jar. It is preferable
 * to use Jinq separated into smaller multiple jars of smaller modules
 * that is available in Maven Central. This single jar version of
 * Jinq is most useful when doing some ad hoc testing of Jinq without
 * a full package dependency management system, especially if you are
 * running with modules disabled. 
 */
module org.jinq.jpa {
   requires jakarta.persistence;
   requires java.sql;
   requires org.jinq.asmrebased;
   exports org.jinq.jpa;
   exports org.jinq.tuples;
   exports org.jinq.orm.internal;
   exports org.jinq.orm.stream;
}