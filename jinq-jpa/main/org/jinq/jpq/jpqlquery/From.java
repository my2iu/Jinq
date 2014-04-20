package org.jinq.jpq.jpqlquery;

public class From implements JPQLFragment
{
   String entityName;
   public static From forEntity(String entityName)
   {
      From from = new From();
      from.entityName = entityName;
      return from;
   }
}
