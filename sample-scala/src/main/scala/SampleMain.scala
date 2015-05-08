
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jinq.jpa.JPAQueryLogger;
import org.jinq.jpa.JinqJPAScalaIteratorProvider;
import org.jinq.orm.stream.scala.JinqConversions._;
import org.jinq.orm.stream.scala.JinqIterator;
import org.jinq.tuples.Pair;

import com.example.jinq.sample.jpa.entities.Customer;
import com.example.jinq.sample.jpa.entities.Item;
import com.example.jinq.sample.jpa.entities.ItemType;
import com.example.jinq.sample.jpa.entities.Lineorder;

 object SampleMain {
  def main(args: Array[String]) {
    // Start running some queries
    new SampleMain().runSampleQueries
  }
}

class SampleMain {
   // Configure Jinq for the given JPA database connection
   val entityManagerFactory = Persistence.createEntityManagerFactory("JPATest");
   SampleDbCreator.createDatabase(entityManagerFactory);
   val streams = new JinqJPAScalaIteratorProvider(entityManagerFactory);

   // Hibernate seems to generate incorrect metamodel data for some types of
   // associations, so we have to manually supply the correct information here.
   streams.registerAssociationAttribute(classOf[Lineorder].getMethod("getItem"), "item", false);
   streams.registerAssociationAttribute(classOf[Lineorder].getMethod("getSale"), "sale", false);

   // Configure Jinq to output the queries it executes
   streams.setHint("queryLogger", new JPAQueryLogger {
      def logQuery(query : String, positionParameters : Map[Integer, Object] ,
            namedParameters : Map[String, Object])
      {
         System.out.println("  " + query);
      }
   });

   var em : EntityManager = _;

   // Helper accessor methods
   
   def customers(): JinqIterator[Customer] = streams.streamAll(em, classOf[Customer]);  
   def items(): JinqIterator[Item] = streams.streamAll(em, classOf[Item]);  
   def lineorders(): JinqIterator[Lineorder] = streams.streamAll(em, classOf[Lineorder]);  
   
   // Actual queries to run
   
   def runSampleQueries()
   {
      val out = System.out;
      em = entityManagerFactory.createEntityManager;

      // Table scan
      out.println("LIST OF CUSTOMERS");
      customers
         .foreach( c => out.println(s"${c.getName} ${c.getCountry} ${c.getSalary}"));
      out.println;

      // Simple filtering based on a field
      out.println("CUSTOMERS FROM THE UK");
      customers
         .where(c => c.getCountry eq "UK")
         .foreach( c => out.println(s"${c.getName} ${c.getCountry}"));
      out.println;

      // Example using lists 
      out.println("CUSTOMERS FROM SWITZERLAND");
      val swiss = customers
         .where(c => c.getCountry eq "Switzerland")
         .toList;
      for (c <- swiss)
         out.println(s"${c.getName} ${c.getCountry}");
      out.println;

      // Simple filtering with a parameter
      // (the parameter must be a basic type and a local variable)
      val param = ItemType.SMALL;
      out.println("ITEMS OF TYPE SMALL");
      items
         .where(i => i.getType eq param)
         .foreach( i => out.println(s"${i.getName} ${i.getType.name}"));
      out.println;

      // More complex filtering involving multiple fields
      out.println("CUSTOMERS WITH DEBT < SALARY");
      customers
         .where(c => c.getDebt < c.getSalary)
         .foreach( c => out.println(s"${c.getName} ${c.getDebt} ${c.getSalary}"));
      out.println;

      // Projection where only a single field is used and a calculation performed
      out.println("HOW MUCH BIGGER IS A CUSTOMER'S DEBT THAN THEIR SALARY");
      customers
         .select(c => (c.getName, c.getDebt - c.getSalary))
         .foreach(pair => out.println(s"${pair._1} ${pair._2}"));
      out.println;

      // Mix of projection and filtering
      out.println("CUSTOMERS WITH A SALARY > 200 AND DEBT < SALARY");
      customers
         .where(c => c.getSalary > 200)
         .select(c => (c.getName, c.getDebt - c.getSalary))
         .where(pair => pair._2 < 0)
         .foreach(pair => out.println(s"${pair._1} ${pair._2}"));
      out.println;

      // More complex filtering using imperative code
      out.println("CUSTOMERS FROM SWITZERLAND WITH SALARY > 250 OR CUSTOMERS FROM USA");
      customers
         .where(c => {
            if (c.getCountry.equals("Switzerland"))
               c.getSalary > 250;
            else
               c.getCountry eq "USA";
         })
         .foreach(c => out.println(s"${c.getName} ${c.getCountry} ${c.getSalary}"));
      out.println;

      // Join across multiple tables using N:1 and 1:1 navigational links
      out.println("CUSTOMERS WHO HAVE PURCHASED WIDGETS IN THE PAST");
      lineorders
         .where(lo => lo.getItem.getName eq "Widgets")
         .select(lo => lo.getSale.getCustomer.getName)
         .foreach(name => out.println(name));
      out.println;
      
      // Sum aggregation
      out.println("TOTAL NUMBER OF WIDGETS SOLD");
      out.println(lineorders
         .where(lo => lo.getItem.getName eq "Widgets")
         .sumInteger(lo => lo.getQuantity) );
      out.println;
      
      // Sorting and limit
      out.println("TOP 3 CUSTOMERS WITH HIGHEST SALARY");
      customers
         .sortedDescendingBy(c => c.getSalary)
         .limit(3)
         .foreach(c => out.println(s"${c.getName} ${c.getSalary}"));
      out.println;

      // Grouping
      out.println("Number of Screws bought by each customer");
      lineorders
         .where(lo => lo.getItem.getName eq "Screws")
         .group( lo => lo.getSale.getCustomer.getName,
               (name : String, los) => los.sumInteger(lo => lo.getQuantity))
         .foreach(pair => out.println(s"${pair._1} ${pair._2}"));
      out.println;

      // Scala list comprehensions
      out.println("Items costing less than 5 dollars")
      val cheapItems = 
         for (i <- items if i.getSaleprice < 5) yield (i.getName, i.getSaleprice)
      cheapItems.foreach(pair => out.println(s"${pair._1} ${pair._2}"));

      em.close;
   }
}
