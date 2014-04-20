package org.jinq.jpq.jpqlquery;

import java.util.ArrayList;
import java.util.List;

public class SelectFromWhere<T> extends JPQLQuery<T>
{
   List<From> froms = new ArrayList<>();

   public String getQueryString()
   {
      QueryGenerationState queryState = new QueryGenerationState();
      
      // Generate aliases for each of the FROM entries
      for (From from: froms)
      {
         queryState.generateFromAlias(from);
      }
      
      // Now generate the query
      String query = "SELECT ";
      if (froms.size() > 0)
      {
         query += queryState.getFromAlias(froms.get(0)) + " ";
      }
      if (froms.size() > 0)
      {
         query += "FROM ";
         boolean isFirst = true;
         for (From from: froms)
         {
            if (!isFirst) query += ", ";
            isFirst = false;
            query += from.entityName + " " + queryState.getFromAlias(froms.get(0));
         }
      }
      return query;
   }

   
//   @Override
//   public JPQLQuery<T> copy()
//   {
//      SelectFromWhere<T> newQuery = new SelectFromWhere<>();
//      newQuery.query = query;
//      return newQuery;
//   }
}
