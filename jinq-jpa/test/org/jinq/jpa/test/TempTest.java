package org.jinq.jpa.test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jinq.jpa.JinqJPAStreamProvider;
import org.jinq.jpa.test.entities.Customer;
import org.jinq.jpa.test.entities.Item;
import org.jinq.orm.stream.JinqStream;

public class TempTest
{
   public static void main(String [] args)
   {
      EntityManagerFactory entityManagerFactory =
            Persistence.createEntityManagerFactory("JPATest");
      JinqJPAStreamProvider streams = new JinqJPAStreamProvider(entityManagerFactory);
      EntityManager em = entityManagerFactory.createEntityManager();

      JinqStream<Customer> customers = streams.streamAll(em, Customer.class);
      customers.forEach(customer -> System.out.println(customer.getName()));

      JinqStream<Item> items = streams.streamAll(em, Item.class);
      items.forEach(item -> System.out.println(item.getName()));

      customers = streams.streamAll(em, Customer.class)
    		  .where(customer -> customer.getDebt() > 100);
   }
}
