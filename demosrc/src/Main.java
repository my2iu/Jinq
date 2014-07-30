import ch.epfl.labos.iu.orm.*;
import ch.epfl.labos.iu.orm.queryll2.QueryllAnalyzer;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.jinq.tuples.Pair;

import com.example.orm.test.entities.*;

public class Main
{
   public static void main(String[] args)
   {
      boolean useDB = false;
      if (args.length > 0)
      {
         if (args[0].equals("-db"))
            useDB = true;
         else if (args[0].equals("-nodb"))
            useDB = false;
      }
      new Main().runTest(0, "", useDB);
   }

   public void runTest(int intParam, String stringParam, boolean sendQueriesToDB)
   {
      // Configure the Object-Relational Mapping tool to be in test
      // mode so that queries are simply displayed and not actual
      // connection to a database is made

      PrintWriter out = new PrintWriter(System.out, true);
      DBManager db;
      if (sendQueriesToDB)
         db = new DBManager(true);
      else
         db = new DBManager(out);

      // Perform bytecode analysis on the classfiles at path "."
      QueryllAnalyzer queryll2 = new QueryllAnalyzer(); 
      queryll2.useRuntimeAnalysis(db);
      
      // Now do some queries!
      //   Note: all queries are evaluated lazily, so at the end of each
      //         query, the size() method is called on the query to 
      //         force the query to be evaluated.
      EntityManager em = db.begin();
      Collection result;

      // Perform a simple projection operation to get the name column 
      // from a customer table
      System.out.println("Names of customers");
      result = em.customerStream()
            .select(c -> c.getName())
            .toList();
      
      // Apply a filter to get the customers from the UK
      System.out.println("Customers from the UK");
      result = em.customerStream()
            .where(c -> c.getCountry().equals("UK"))
            .toList();

      // Filter with a parameter
      System.out.println("Customers from a country to be specified");
      result = em.customerStream()
            .where(c -> c.getCountry().equals(stringParam))
            .toList();

      // Apply a filter, project to get two columns, and filter based
      // on those two columns
      System.out.println("Customers named John from the UK");
      result = em.customerStream()
            .where(c -> c.getCountry().equals("UK"))
            .select(c -> new Pair<String, String>(c.getName(), 
                                                  c.getCountry()))
            .where(pair -> pair.getOne().equals("John"))
            .toList();

      // Conditionals inside of a projection, resulting in a CASE..WHEN  
      System.out.println("Customers from the UK or not");
      result = em.customerStream()
            .select( c -> c.getCountry().equals("UK")
                  ? "UK" : "Not from UK")
            .toList();

      // If statements and variables inside a projection  
      System.out.println("Customers from the US are relabelled " +
      		"as coming from USA");
      result = em.customerStream()
            .select(c -> {
               String country = c.getCountry();
               if (country.equals("US"))
                  return "USA";
               else
                  return country;
               })
            .toList();

      // Join to another table
      System.out.println("Join of customer and sale tables");
      result = em.customerStream()
            .join(c -> em.saleStream())
            .select(pair -> new Pair<>(pair.getOne().getName(),
                                    pair.getTwo().getDate()))
            .toList();

      // Join using a N:1 navigational link
      System.out.println("Sales made to Bob");
      result = em.saleStream()
            .where(s -> s.getPurchaser().getName().equals("Bob"))
            .toList();

      // Simple N:1 navigational link used with filtering and projection
      System.out.println("Sales made to Bob (some fields)"); 
      result = em.saleStream()
            .where(s -> s.getPurchaser().getName().equals("Bob"))
            .select(s -> new Pair<>(s.getPurchaser().getName(),
                                    s.getSaleId()))
            .toList();

      // Aggregation operations
      System.out.println("Sum of all line order quantities");
      long total = em.lineOrderStream()
            .sumInteger(lo -> lo.getQuantity());

      // Calculate more than one aggregate
      System.out.println("Sum of all line order quantities " +
      		"and number of line orders");
      Pair pairResult = em.lineOrderStream()
            .aggregate(data -> data.sumInteger(lo -> lo.getQuantity()),
                       data -> data.sumInteger(lo -> 1));

      // Some math inside a select
      System.out.println("Profit on each item");
      result = em.itemStream()
            .select(i -> new Pair<>(i.getItemId(),
                                    i.getSalePrice() - i.getPurchasePrice()))
            .toList();

      // Group together results
      System.out.println("Customers and the number of sales to them");
      result = em.saleStream()
            .group(
                  s -> s.getPurchaser().getCustomerId(),
                  (key, sales) -> sales.sumInteger(sale -> 1))
            .toList();
      
      // Limited subquery support (so far)
      System.out.println("Most recent sale");
      result = em.saleStream()
            .where((s) -> em.allSale().maxInt(ss -> ss.getSaleId())
                  == s.getSaleId())
            .toList();
 
      // Sorting of results
      System.out.println("Sorted list of customer names");
      result = em.customerStream()
            .select(c -> c.getName())
            .sortedBy(name -> name)
            .toList();
      
      // Simple query that is not possible with a simple ORM.
      System.out.println("If statements in a where()");
      result = em.customerStream()
            .where( c -> {
               if (c.getSalary() > 50)
                  return c.getSalary() > c.getDebt();
               else
                  return c.getSalary() > 2 * c.getDebt();
            })
            .toList();
      
      // Query that cannot be translated into SQL
//      result = em.customerStream()
//         .select(o -> o.hashCode());
     
      // End things now
      db.end(em, true);
   }
}
