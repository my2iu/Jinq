package org.jinq.jpa.test.entities;

import java.io.Serializable;

public class PhoneNumber implements Serializable
{
   private static final long serialVersionUID = -8237657991696527948L;
   
   public final String countryCode;
   public final String areaCode;
   public final String number;
   
   public PhoneNumber() 
   {
      countryCode = "1";
      areaCode = "0";
      number = "0";
   };
   public PhoneNumber(String countryCode, String areaCode, String number) 
   {
      this.countryCode = countryCode;
      this.areaCode = areaCode;
      this.number = number;
   }
   
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((areaCode == null) ? 0 : areaCode.hashCode());
      result = prime * result
            + ((countryCode == null) ? 0 : countryCode.hashCode());
      result = prime * result + ((number == null) ? 0 : number.hashCode());
      return result;
   }
   
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      PhoneNumber other = (PhoneNumber) obj;
      if (areaCode == null)
      {
         if (other.areaCode != null)
            return false;
      } else if (!areaCode.equals(other.areaCode))
         return false;
      if (countryCode == null)
      {
         if (other.countryCode != null)
            return false;
      } else if (!countryCode.equals(other.countryCode))
         return false;
      if (number == null)
      {
         if (other.number != null)
            return false;
      } else if (!number.equals(other.number))
         return false;
      return true;
   }
   @Override
   public String toString()
   {
      return countryCode + "-" + areaCode + "-" + number;
   }
}
