package org.jinq.jpa.test.entities;

import javax.persistence.Embeddable;

@Embeddable
public class CreditCard
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
