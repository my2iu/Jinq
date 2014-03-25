import ch.epfl.labos.iu.orm.*;
import ch.epfl.labos.iu.orm.queryll2.QueryllAnalyzer;

import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

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
      DBSet result;

      // Perform a simple projection operation to get the name column 
      // from a customer table
      System.out.println("Names of customers");
      result = em.allCustomer()
         .select(c -> c.getName());
      result.size();
      
      // Apply a filter to get the customers from the UK
      System.out.println("Customers from the UK");
      result = em.allCustomer()
         .where(c -> c.getCountry().equals("UK"));
      result.size();

      // Filter with a parameter
      System.out.println("Customers from a country to be specified");
      result = em.allCustomer()
         .where( c -> c.getCountry().equals(stringParam));
      result.size();

      // Apply a filter, project to get two columns, and filter based
      // on those two columns
      System.out.println("Customers named John from the UK");
      result = em.allCustomer()
         .where(c -> c.getCountry().equals("UK"))
         .select(c -> new Pair<String, String>(c.getName(), 
                                               c.getCountry()))
         .where(pair -> pair.getOne().equals("John"));
      result.size();

      // Conditionals inside of a projection, resulting in a CASE..WHEN  
      System.out.println("Customers from the UK or not");
      result = em.allCustomer()
         .select( c -> c.getCountry().equals("UK")
               ? "UK" : "Not from UK");
      result.size();

      // If statements and variables inside a projection  
      System.out.println("Customers from the US are relabelled " +
      		"as coming from USA");
      result = em.allCustomer()
         .select(c -> {
            String country = c.getCountry();
            if (country.equals("US"))
               return "USA";
            else
               return country;
            });
      result.size();

      // Join to another table
      System.out.println("Join of customer and sale tables");
      result = em.allCustomer()
         .join(c -> em.allSale())
         .select(pair -> new Pair<>(pair.getOne().getName(),
                                    pair.getTwo().getDate()));
      result.size();

      // Join using a N:1 navigational link
      System.out.println("Sales made to Bob");
      result = em.allSale()
         .where(s -> s.getPurchaser().getName().equals("Bob"));
      result.size();

      // Simple N:1 navigational link used with filtering and projection
      System.out.println("Sales made to Bob (some fields)"); 
      result = em.allSale()
         .where(s -> s.getPurchaser().getName().equals("Bob"))
         .select(s -> new Pair<>(s.getPurchaser().getName(),
                                 s.getSaleId()));
      result.size();

      // Aggregation operations
      System.out.println("Sum of all line order quantities");
      int total = em.allLineOrder()
         .sumInt(lo -> lo.getQuantity());

      // Calculate more than one aggregate
      System.out.println("Sum of all line order quantities " +
      		"and number of line orders");
      Pair pairResult = em.allLineOrder()
         .selectAggregates(loset ->
            new Pair<>(loset.sumInt(lo -> lo.getQuantity()),
                       loset.sumInt(lo -> 1)));

      // Some math inside an aggregation
      System.out.println("Profit on each item");
      result = em.allItem()
         .select(i -> new Pair<>(i.getItemId(),
                                 i.getSalePrice() - i.getPurchasePrice()));
      result.size();

      // Group together results
      System.out.println("Customers and the number of sales to them");
      result = em.allSale()
         .group(
                s -> s.getPurchaser().getCustomerId(),
                (key, sales) -> sales.sumInt(sale -> 1));
      result.size();
      
      // Limited subquery support (so far)
      System.out.println("Most recent sale");
      result = em.allSale()
         .where((s) -> em.allSale().maxInt(ss -> ss.getSaleId())
                          == s.getSaleId());
      result.size();
 
      // Sorting of results
      System.out.println("Sorted list of customer names");
      result = em.allCustomer()
         .select(c -> c.getName())
         .sortedByStringAscending(name -> name);
      result.size();
      
      // Simple query that is not possible with a simple ORM.
      System.out.println("If statements in a where()");
      result = em.allCustomer()
         .where( c -> {
            if (c.getSalary() > 50)
               return c.getSalary() > c.getDebt();
            else
               return c.getSalary() > 2 * c.getDebt();
         });
      result.size();

      // Used for testing streams.
      List resultList;

      // Experiment with streams
      System.out.println("Streams: Names of customers");
      resultList = em.customerStream()
            .select(c -> c.getName())
            .toList();
      System.out.println("Streams: Customers from the UK");
      resultList = em.customerStream()
         .where(c -> c.getCountry().equals("UK"))
         .select(c -> c.getCustomerId())
         .toList();
      System.out.println("Streams: Customers from a country to be specified");
      resultList = em.customerStream()
         .where( c -> c.getCountry().equals(stringParam))
         .toList();
      result.size();
      System.out.println("Streams: Join of customer and sale tables");
      resultList = em.customerStream()
         .join(c -> em.saleStream())
         .select(pair -> new Pair<>(pair.getOne().getName(),
                                    pair.getTwo().getDate()))
         .toList();

      
      
      // Query that cannot be translated into SQL
//      result = em.allCustomer()
//         .select(o -> o.hashCode());
     
      // End things now
      db.end(em, true);
   }
}
