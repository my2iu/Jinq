package org.jinq.jooq;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.*;
import org.jooq.meta.jaxb.Jdbc;

class JooqCodeGen
{
   static String dbName = JinqJooqTest.class.getName(); 

   public static void main(String[] args) throws Exception
   {
     Connection con = DriverManager.getConnection("jdbc:derby:memory:" + dbName + ";create=true");
     CreateJdbcDb createDb = new CreateJdbcDb(con);
     createDb.createDatabase();
     
     Configuration configuration = new org.jooq.meta.jaxb.Configuration()
     .withJdbc(new Jdbc()
       .withDriver("org.apache.derby.jdbc.EmbeddedDriver")
       .withUrl("jdbc:derby:memory:" + dbName)
       .withUser("")
       .withPassword("")
     )
     .withGenerator(new Generator()
       .withDatabase(new Database()
         .withName("org.jooq.meta.derby.DerbyDatabase")
         .withIncludes(".*")
         .withExcludes("")
         .withInputSchema("APP")
       )

       .withTarget(new Target()
         .withPackageName("org.jinq.jooq.test.generated")
         .withDirectory("generated")
       )
     );
     GenerationTool.generate(configuration);
  }
}
