package org.jinq.jpa;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.EntityManager;

import org.jinq.jpa.test.entities.Customer;
import org.jinq.jpa.test.entities.Item;
import org.jinq.jpa.test.entities.Lineorder;
import org.jinq.jpa.test.entities.Sale;
import org.jinq.jpa.test.entities.Supplier;

public class CreateJpaDb
{
   EntityManager em;

   public CreateJpaDb(EntityManager em)
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
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      // SQL and JPA has some issues with timezones, so we'll manually
      // set the years and stuff for dates using deprecated methods to 
      // ensure we consistently get certain values in the database.
      java.sql.Date sqlDate = new java.sql.Date(year, month, day);
      java.sql.Time sqlTime = new java.sql.Time(hour, 0, 0);
      java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(year, month, day, hour, 0, 0, 0);
      
      Sale s = new Sale();
      s.setDate(date);
      s.setCalendar(cal);
      s.setSqlDate(sqlDate);
      s.setSqlTime(sqlTime);
      s.setSqlTimestamp(sqlTimestamp);
      s.setCustomer(customer);
      return s;
   }

   private Item createItem(String name, int purchasePrice, int salePrice)
   {
      Item i = new Item();
      i.setName(name);
      i.setSaleprice(salePrice);
      i.setPurchaseprice(purchasePrice);
      return i;
   }
   
   private Lineorder addLineorder(Sale s, Item item, int quantity)
   {
      Lineorder lo = new Lineorder();
      s.addLineorder(lo);
      item.addLineorder(lo);
      lo.setQuantity(quantity);
      return lo;
   }
   
   private Supplier createSupplier(String name, String country, long revenue, boolean hasFreeShipping)
   {
      Supplier s = new Supplier();
      s.setName(name);
      s.setCountry(country);
      s.setRevenue(revenue);
      s.setHasFreeShipping(hasFreeShipping);
      return s;
   }
   
   void createDatabase()
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

      Item widgets = createItem("Widgets", 5, 10);
      Item wudgets = createItem("Wudgets", 2, 3);
      Item talent = createItem("Talent", 6, 1000);
      Item lawnmowers = createItem("Lawnmowers", 100, 102);
      Item screws = createItem("Screws", 1, 2);
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
      
      em.persist(addLineorder(s1, widgets, 1));
      em.persist(addLineorder(s2, wudgets, 2));
      em.persist(addLineorder(s2, screws, 1));
      em.persist(addLineorder(s2, lawnmowers, 2));
      em.persist(addLineorder(s3, screws, 1000));
      em.persist(addLineorder(s4, widgets, 200));
      em.persist(addLineorder(s5, talent, 6));
      em.persist(addLineorder(s6, widgets, 2));
      em.persist(addLineorder(s6, wudgets, 2));
      em.persist(addLineorder(s6, lawnmowers, 2));
      em.persist(addLineorder(s6, screws, 7));
      
      Supplier s = createSupplier("HW Supplier", "Canada", 500, false);
      s.setItems(Arrays.asList(widgets, wudgets, screws));
      em.persist(s);
      s = createSupplier("Talent Agency", "USA", 1000, true);
      s.setItems(Arrays.asList(talent));
      em.persist(s);
      s = createSupplier("Conglomerate", "Switzerland", 10000000L, false);
      s.setItems(Arrays.asList(widgets, lawnmowers));
      em.persist(s);
      
      em.getTransaction().commit();
   }
}
