package org.jinq.jpq.jpqlquery;


/**
 * Data structure used to represent JPQL queries and the conversions
 * needed to parse results into a form usable by Jinq.
 */
public class JPQLQuery<T>
{
   String query;
   
   public static <U> JPQLQuery<U> findAllEntities(String entityName)
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

   // TODO: If I code things carefully, maybe this method isn't needed.
   public JPQLQuery<T> copy()
   {
      JPQLQuery<T> newQuery = new JPQLQuery<>();
      newQuery.query = query;
      return newQuery;
   }
}
