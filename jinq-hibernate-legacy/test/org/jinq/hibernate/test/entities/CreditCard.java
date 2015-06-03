package org.jinq.hibernate.test.entities;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class CreditCard implements Serializable
{
   long number;
   int cvv;
   String name;
   
   public long getNumber()
   {
      return number;
   }
   
   public void setNumber(long number)
   {
      this.number = number;
   }
   
   public int getCvv()
   {
      return cvv;
   }
   
   public void setCvv(int cvv)
   {
      this.cvv = cvv;
   }
   
   public String getName()
   {
      return name;
   }
   
   public void setName(String name)
   {
      this.name = name;
   }
}
