
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.example.jinq.sample.jpa.entities.Customer;
import com.example.jinq.sample.jpa.entities.Item;
import com.example.jinq.sample.jpa.entities.ItemType;
import com.example.jinq.sample.jpa.entities.Lineorder;
import com.example.jinq.sample.jpa.entities.Sale;
import com.example.jinq.sample.jpa.entities.Supplier;

public class SampleDbCreator
{
   EntityManager em;

   public SampleDbCreator(EntityManager em)
   {
      this.em = em;
   }
   
   private Customer createCustomer(String name, String country, int debt, int salary)
   {
      Customer c = new Customer();
      c.setName(name);
      c.setDebt(debt);
      c.setSalary(salary);
      c.setCountry(country);
      return c;
   }
   
   private Sale createSale(Customer customer, int year)
   {
      return createSale(customer, year, 1, 1, 1);
   }

   private Sale createSale(Customer customer, int year, int month, int day, int hour)
   {
      Instant instant = LocalDateTime.of(year, month, day, hour, 0).toInstant(ZoneOffset.UTC);
      Date date = Date.from(instant);
      
      Sale s = new Sale();
      s.setDate(date);
      s.setCustomer(customer);
      return s;
   }

   private Item createItem(String name, ItemType type, int purchasePrice, int salePrice)
   {
      Item i = new Item();
      i.setName(name);
      i.setType(type);
      i.setSaleprice(salePrice);
      i.setPurchaseprice(purchasePrice);
      return i;
   }
   
   private Lineorder addLineorder(Sale s, Item item, int quantity, int transactionConfirmation)
   {
      Lineorder lo = new Lineorder();
      s.addLineorder(lo);
      item.addLineorder(lo);
      lo.setQuantity(quantity);
      lo.setTotal(BigDecimal.valueOf(quantity * item.getPurchaseprice()));
      lo.setTransactionConfirmation(BigInteger.valueOf(transactionConfirmation));
      return lo;
   }
   
   private Supplier createSupplier(String name, String country, long revenue, boolean hasFreeShipping)
   {
      Supplier s = new Supplier();
      s.setName(name);
      s.setCountry(country);
      s.setRevenue(revenue);
      s.setHasFreeShipping(hasFreeShipping);
      s.setSignature(name.getBytes(Charset.forName("UTF-8")));
      return s;
   }
   
   static void createDatabase(EntityManagerFactory entityManagerFactory)
   {
      EntityManager em = entityManagerFactory.createEntityManager();
      try {
         new SampleDbCreator(em).go();
      } 
      finally 
      {
         em.close();
      }
      
   }
   
   void go()
   {
      em.getTransaction().begin();

      Customer alice = createCustomer("Alice", "Switzerland", 100, 200);
      Customer bob = createCustomer("Bob", "Switzerland", 200, 300);
      Customer carol = createCustomer("Carol", "USA", 300, 250);
      Customer dave = createCustomer("Dave", "UK", 100, 500);
      Customer eve = createCustomer("Eve", "Canada", 10, 30); 
      em.persist(alice);
      em.persist(bob);
      em.persist(carol);
      em.persist(dave);
      em.persist(eve);

      Supplier hw = createSupplier("HW Supplier", "Canada", 500, false);
      Supplier talentSupply = createSupplier("Talent Agency", "USA", 1000, true);
      Supplier conglomerate = createSupplier("Conglomerate", "Switzerland", 10000000L, false);
      em.persist(hw);
      em.persist(talentSupply);
      em.persist(conglomerate);

      Item widgets = createItem("Widgets", ItemType.SMALL, 5, 10);
      Item wudgets = createItem("Wudgets", ItemType.SMALL, 2, 3);
      Item talent = createItem("Talent", ItemType.OTHER, 6, 1000);
      Item lawnmowers = createItem("Lawnmowers", ItemType.BIG, 100, 102);
      Item screws = createItem("Screws", ItemType.SMALL, 1, 2);
      widgets.setSuppliers(Arrays.asList(hw, conglomerate));
      wudgets.setSuppliers(Arrays.asList(hw));
      talent.setSuppliers(Arrays.asList(talentSupply));
      lawnmowers.setSuppliers(Arrays.asList(conglomerate));
      screws.setSuppliers(Arrays.asList(hw));
      em.persist(widgets);
      em.persist(wudgets);
      em.persist(talent);
      em.persist(lawnmowers);
      em.persist(screws);

      em.flush();
      
      Sale s1 = createSale(alice, 2005, 2, 2, 10);
      Sale s2 = createSale(alice, 2004);
      Sale s3 = createSale(carol, 2003);
      Sale s4 = createSale(carol, 2004, 2, 2, 5);
      Sale s5 = createSale(dave, 2001);
      Sale s6 = createSale(eve, 2005);
      em.persist(s1);
      em.persist(s2);
      em.persist(s3);
      em.persist(s4);
      em.persist(s5);
      em.persist(s6);
      
      em.flush();
      
      em.persist(addLineorder(s1, widgets, 1, 1000));
      em.persist(addLineorder(s2, wudgets, 2, 2000));
      em.persist(addLineorder(s2, screws, 1, 3000));
      em.persist(addLineorder(s2, lawnmowers, 2, 4000));
      em.persist(addLineorder(s3, screws, 1000, 5000));
      em.persist(addLineorder(s4, widgets, 200, 6000));
      em.persist(addLineorder(s5, talent, 6, 7000));
      em.persist(addLineorder(s6, widgets, 2, 8000));
      em.persist(addLineorder(s6, wudgets, 2, 9000));
      em.persist(addLineorder(s6, lawnmowers, 2, 10000));
      em.persist(addLineorder(s6, screws, 7, 11000));

      em.getTransaction().commit();
   }
}
