

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jinq.jpa.JPAQueryLogger;
import org.jinq.jpa.JinqJPAStreamProvider;
import org.jinq.orm.stream.JinqStream;
import org.jinq.tuples.Pair;

import com.example.jinq.sample.jpa.entities.Customer;
import com.example.jinq.sample.jpa.entities.Sale;

public class SampleMain
{
   static EntityManagerFactory entityManagerFactory;
   static JinqJPAStreamProvider streams;

   EntityManager em;

   public static void main(String[] args)
   {
      entityManagerFactory = Persistence.createEntityManagerFactory("JPATest");
      SampleDbCreator.createDatabase(entityManagerFactory);
      streams = new JinqJPAStreamProvider(entityManagerFactory);
      streams.setHint("queryLogger", new JPAQueryLogger() {
         @Override public void logQuery(String query, Map<Integer, Object> positionParameters,
               Map<String, Object> namedParameters)
         {
            System.out.println(query);
         }
      });

      new SampleMain().go();
   }

   public void go()
   {
      em = entityManagerFactory.createEntityManager();
      JinqStream<Customer> customers = streams.streamAll(em, Customer.class);
      List<Customer> customerList = customers.toList();
      for (Customer c: customerList)
         System.out.println(c.getName());
      em.close();
   }
}
