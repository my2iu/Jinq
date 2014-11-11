
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jinq.jpa.JPAQueryLogger;
import org.jinq.jpa.JinqJPAScalaStreamProvider;
import org.jinq.orm.stream.scala.JinqConversions._;
import org.jinq.orm.stream.scala.JinqScalaStream;
import org.jinq.tuples.Pair;

import com.example.jinq.sample.jpa.entities.Customer;
import com.example.jinq.sample.jpa.entities.Item;
import com.example.jinq.sample.jpa.entities.ItemType;
import com.example.jinq.sample.jpa.entities.Lineorder;

 object SampleMain {
  def main(args: Array[String]) {
    new SampleMain().start()
  }
}

class SampleMain {
   var entityManagerFactory : EntityManagerFactory = _;
   var streams : JinqJPAScalaStreamProvider = _;

   var em : EntityManager = _;

   def start()
   {
      // Configure Jinq for the given JPA database connection
      entityManagerFactory = Persistence.createEntityManagerFactory("JPATest");
      SampleDbCreator.createDatabase(entityManagerFactory);
      streams = new JinqJPAScalaStreamProvider(entityManagerFactory);
      
      // Hibernate seems to generate incorrect metamodel data for some types of
      // associations, so we have to manually supply the correct information here.
      streams.registerAssociationAttribute(classOf[Lineorder].getMethod("getItem"), "item", false);
      streams.registerAssociationAttribute(classOf[Lineorder].getMethod("getSale"), "sale", false);
      
      // Configure Jinq to output the queries it executes
      streams.setHint("queryLogger", new JPAQueryLogger() {
         def logQuery(query : String, positionParameters : Map[Integer, Object] ,
               namedParameters : Map[String, Object])
         {
            System.out.println("  " + query);
         }
      });

      // Start running some queries
      runSampleQueries();
   }

   // Helper accessor methods
   
   def customers(): JinqScalaStream[Customer] = { return streams.streamAll(em, classOf[Customer]); }  
   def items(): JinqScalaStream[Item] = { return streams.streamAll(em, classOf[Item]); }  
   def lineorders(): JinqScalaStream[Lineorder] = { return streams.streamAll(em, classOf[Lineorder]); }  
   
   // Actual queries to run
   
   def runSampleQueries()
   {
      val out = System.out;
      em = entityManagerFactory.createEntityManager();

      // Table scan
      out.println("LIST OF CUSTOMERS");
      customers()
      	 .toIterator()
         .foreach( c => out.println(c.getName() + " " + c.getCountry() + " " + c.getSalary()));
      out.println();

      // Simple filtering based on a field
      out.println("CUSTOMERS FROM THE UK");
      customers()
         .where(c => c.getCountry() eq "UK")
         .toIterator()
         .foreach( c => out.println(c.getName() + " " + c.getCountry()));
      out.println();

      // Example using lists 
      out.println("CUSTOMERS FROM SWITZERLAND");
      val swiss = customers()
         .where(c => c.getCountry() eq "Switzerland")
         .toList();
      for (c <- swiss)
         out.println(c.getName() + " " + c.getCountry());
      out.println();

      // Simple filtering with a parameter
      // (the parameter must be a basic type and a local variable)
      val param = ItemType.SMALL;
      out.println("ITEMS OF TYPE SMALL");
      items()
         .where(i => i.getType() eq param)
         .toIterator
         .foreach( i => out.println(i.getName() + " " + i.getType().name()));
      out.println();

      // More complex filtering involving multiple fields
      out.println("CUSTOMERS WITH DEBT < SALARY");
      customers()
         .where(c => c.getDebt() < c.getSalary())
         .toIterator()
         .foreach( c => out.println(c.getName() + " " + c.getDebt() + " " + c.getSalary()));
      out.println();

      // Projection where only a single field is used and a calculation performed
      out.println("HOW MUCH BIGGER IS A CUSTOMER'S DEBT THAN THEIR SALARY");
      customers()
         .select(c => (c.getName(), c.getDebt() - c.getSalary()))
         .toIterator()
         .foreach(pair => out.println(pair._1 + " " + pair._2));
      out.println();

      // Mix of projection and filtering
      out.println("CUSTOMERS WITH A SALARY > 200 AND DEBT < SALARY");
      customers()
         .where(c => c.getSalary() > 200)
         .select(c => (c.getName(), c.getDebt() - c.getSalary()))
         .where(pair => pair._2 < 0)
         .toIterator()
         .foreach(pair => out.println(pair._1 + " " + pair._2));
      out.println();

      // More complex filtering using imperative code
      out.println("CUSTOMERS FROM SWITZERLAND WITH SALARY > 250 OR CUSTOMERS FROM USA");
      customers()
         .where(c => {
            if (c.getCountry().equals("Switzerland"))
               c.getSalary() > 250;
            else
               c.getCountry() eq "USA";
         })
         .toIterator()
         .foreach(c => out.println(c.getName() + " " + c.getCountry() + " " + c.getSalary()));
      out.println();

      // Join across multiple tables using N:1 and 1:1 navigational links
      out.println("CUSTOMERS WHO HAVE PURCHASED WIDGETS IN THE PAST");
      lineorders()
         .where(lo => lo.getItem().getName() eq "Widgets")
         .select(lo => lo.getSale().getCustomer().getName())
         .toIterator()
         .foreach(name => out.println(name));
      out.println();
      
      // Sum aggregation
      out.println("TOTAL NUMBER OF WIDGETS SOLD");
      out.println(lineorders()
         .where(lo => lo.getItem().getName() eq "Widgets")
         .sumInteger(lo => lo.getQuantity()) );
      out.println();
      
      // Sorting and limit
      out.println("TOP 3 CUSTOMERS WITH HIGHEST SALARY");
      customers()
         .sortedDescendingBy(c => c.getSalary())
         .limit(3)
         .toIterator()
         .foreach(c => out.println(c.getName() + " " + c.getSalary()));
      out.println();

      // Grouping
//      out.println("Number of Screws bought by each customer");
//      lineorders()
//         .where(lo => lo.getItem().getName() eq "Screws")
//         .group( lo => lo.getSale().getCustomer().getName(),
//               (name, los) => los.sumInteger(lo => lo.getQuantity()))
//         .foreach(pair => out.println(pair._1 + " " + pair._2));
//      out.println();

      em.close();
   }
}
