package org.jinq.jpa.jpqlquery;

import java.util.ArrayList;
import java.util.List;

public class SelectFromWhere<T> extends JPQLQuery<T>
{
   public ColumnExpressions<T> cols;
   public List<From> froms = new ArrayList<>();
   public Expression where;

   public String getQueryString()
   {
      QueryGenerationState queryState = new QueryGenerationState();
      
      // Generate aliases for each of the FROM entries
      for (From from: froms)
      {
         queryState.generateFromAlias(from);
      }
      
      // Now generate the query
      String query = "";
      if (cols.getNumColumns() > 0)
      {
         query += "SELECT ";
         boolean isFirst = true;
         for (Expression col: cols.columns)
         {
            if (!isFirst) query += ", ";
            isFirst = false;
            queryState.queryString = "";
            col.generateQuery(queryState);
            query += queryState.queryString;
         }
      }
      if (froms.size() > 0)
      {
         query += " FROM ";
         boolean isFirst = true;
         for (From from: froms)
         {
            if (!isFirst) query += ", ";
            isFirst = false;
            query += from.entityName + " " + queryState.getFromAlias(froms.get(0));
         }
      }
      if (where != null)
      {
         query += " WHERE ";
         queryState.queryString = "";
         where.generateQuery(queryState);
         query += queryState.queryString;
      }
      return query;
   }

   @Override
   public RowReader<T> getRowReader()
   {
      return cols.reader;
   }
   
//   @Override
//   public JPQLQuery<T> copy()
//   {
//      SelectFromWhere<T> newQuery = new SelectFromWhere<>();
//      newQuery.query = query;
//      return newQuery;
//   }
}
