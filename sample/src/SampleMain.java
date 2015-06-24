

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jinq.jpa.JPAQueryLogger;
import org.jinq.jpa.JinqJPAStreamProvider;
import org.jinq.orm.stream.JinqStream;
import org.jinq.tuples.Pair;

import com.example.jinq.sample.jpa.entities.Customer;
import com.example.jinq.sample.jpa.entities.Item;
import com.example.jinq.sample.jpa.entities.ItemType;
import com.example.jinq.sample.jpa.entities.Lineorder;

public class SampleMain
{
   private static EntityManagerFactory entityManagerFactory;
   private static JinqJPAStreamProvider streams;

   private EntityManager em;

   public static void main(String[] args) throws Exception
   {
      // Configure Jinq for the given JPA database connection
      entityManagerFactory = Persistence.createEntityManagerFactory("JPATest");
      SampleDbCreator.createDatabase(entityManagerFactory);
      streams = new JinqJPAStreamProvider(entityManagerFactory);
      
      // Hibernate seems to generate incorrect metamodel data for some types of
      // associations (specifically, the @ManyToOne relation), so we have to 
      // manually supply the correct information to Jinq here. This is not needed
      // for EclipseLink. 
      streams.registerAssociationAttribute(Lineorder.class.getMethod("getItem"), "item", false);
      streams.registerAssociationAttribute(Lineorder.class.getMethod("getSale"), "sale", false);
      
      // Configure Jinq to output the queries it executes
      streams.setHint("queryLogger", new JPAQueryLogger() {
         @Override public void logQuery(String query, Map<Integer, Object> positionParameters,
               Map<String, Object> namedParameters)
         {
            System.out.println("  " + query);
         }
      });

      // Start running some queries
      new SampleMain().runSampleQueries();
   }

   // Helper accessor methods
   
   private JinqStream<Customer> customers() { return streams.streamAll(em, Customer.class); }  
   private JinqStream<Item> items() { return streams.streamAll(em, Item.class); }  
   private JinqStream<Lineorder> lineorders() { return streams.streamAll(em, Lineorder.class); }  
   
   // Actual queries to run
   
   public void runSampleQueries()
   {
      PrintStream out = System.out;
      em = entityManagerFactory.createEntityManager();

      // Table scan
      out.println("LIST OF CUSTOMERS");
      customers()
         .forEach( c -> out.println(c.getName() + " " + c.getCountry() + " " + c.getSalary()));
      out.println();
      
      // Simple filtering based on a field
      out.println("CUSTOMERS FROM THE UK");
      customers()
         .where(c -> c.getCountry().equals("UK"))
         .forEach( c -> out.println(c.getName() + " " + c.getCountry()));
      out.println();

      // Example using lists 
      out.println("CUSTOMERS FROM SWITZERLAND");
      List<Customer> swiss = customers()
         .where(c -> c.getCountry().equals("Switzerland"))
         .toList();
      for (Customer c: swiss)
         out.println(c.getName() + " " + c.getCountry());
      out.println();
      
      // Simple filtering with a parameter
      // (the parameter must be a basic type and a local variable)
      ItemType param = ItemType.SMALL;
      out.println("ITEMS OF TYPE SMALL");
      items()
         .where(i -> i.getType() == param)
         .forEach( i -> out.println(i.getName() + " " + i.getType().name()));
      out.println();

      // More complex filtering involving multiple fields
      out.println("CUSTOMERS WITH DEBT < SALARY");
      customers()
         .where(c -> c.getDebt() < c.getSalary())
         .forEach( c -> out.println(c.getName() + " " + c.getDebt() + " " + c.getSalary()));
      out.println();

      // Projection where only a single field is used and a calculation performed
      out.println("HOW MUCH BIGGER IS A CUSTOMER'S DEBT THAN THEIR SALARY");
      customers()
         .select(c -> new Pair<>(c.getName(), c.getDebt() - c.getSalary()))
         .forEach(pair -> out.println(pair.getOne() + " " + pair.getTwo()));
      out.println();

      // Mix of projection and filtering
      out.println("CUSTOMERS WITH A SALARY > 200 AND DEBT < SALARY");
      customers()
         .where(c -> c.getSalary() > 200)
         .select(c -> new Pair<>(c.getName(), c.getDebt() - c.getSalary()))
         .where(pair -> pair.getTwo() < 0)
         .forEach(pair -> out.println(pair.getOne() + " " + pair.getTwo()));
      out.println();

      // More complex filtering using imperative code
      out.println("CUSTOMERS FROM SWITZERLAND WITH SALARY > 250 OR CUSTOMERS FROM USA");
      customers()
         .where(c -> {
            if (c.getCountry().equals("Switzerland"))
               return c.getSalary() > 250;
            else
               return c.getCountry().equals("USA");
         })
         .forEach(c -> out.println(c.getName() + " " + c.getCountry() + " " + c.getSalary()));
      out.println();

      // Join across multiple tables using N:1 and 1:1 navigational links
      out.println("CUSTOMERS WHO HAVE PURCHASED WIDGETS IN THE PAST");
      lineorders()
         .where(lo -> lo.getItem().getName().equals("Widgets"))
         .select(lo -> lo.getSale().getCustomer().getName())
         .forEach(name -> out.println(name));
      out.println();
      
      // Sum aggregation
      out.println("TOTAL NUMBER OF WIDGETS SOLD");
      out.println(lineorders()
         .where(lo -> lo.getItem().getName().equals("Widgets"))
         .sumInteger(lo -> lo.getQuantity()) );
      out.println();
      
      // Sorting and limit
      out.println("TOP 3 CUSTOMERS WITH HIGHEST SALARY");
      customers()
         .sortedDescendingBy(c -> c.getSalary())
         .limit(3)
         .forEach(c -> out.println(c.getName() + " " + c.getSalary()));
      out.println();

      // Grouping
      out.println("NUMBER OF SCREWS BOUGHT BY EACH CUSTOMER");
      lineorders()
         .where(lo -> lo.getItem().getName().equals("Screws"))
         .group( lo -> lo.getSale().getCustomer().getName(),
               (name, los) -> los.sumInteger(lo -> lo.getQuantity()))
         .forEach(pair -> out.println(pair.getOne() + " " + pair.getTwo()));
      out.println();

      em.close();
   }
}
