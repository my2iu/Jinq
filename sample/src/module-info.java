module com.example.jinq.sample {
   requires jakarta.persistence;
   requires java.sql;
   requires org.jinq.jpa;
   exports com.example.jinq.sample;
   exports com.example.jinq.sample.jpa.entities;
   opens com.example.jinq.sample.jpa.entities;
}