package org.jinq.jpa;


/**
 * Data structure used to represent JPQL queries and the conversions
 * needed to parse results into a form usable by Jinq.
 */
public class JPQLQuery<T>
{
   String query;
   
   public static <U> JPQLQuery<U> findAllEntity(String entityName)
   {
      JPQLQuery<U> query = new JPQLQuery<>();
      query.query = "SELECT c FROM " + entityName + " c"; 
      return query;
   }
   
   public JPQLQuery()
   {
   }

   public String getQueryString()
   {
      return query;
   }

}
