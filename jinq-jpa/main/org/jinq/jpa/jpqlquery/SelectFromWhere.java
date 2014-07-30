package org.jinq.jpa.jpqlquery;

import java.util.ArrayList;
import java.util.List;

public class SelectFromWhere<T> extends JPQLQuery<T>
{
   public ColumnExpressions<T> cols;
   public List<From> froms = new ArrayList<>();
   public Expression where;
   public boolean isAggregated = false;

   /**
    * After a JPQL query is generated, this stores the resulting query string.  
    */
   private String queryString;
   /**
    * After a JPQL query is generated, this stores parameters that need to be 
    * filled-in, in the query  
    */
   private List<GeneratedQueryParameter> queryParameters;
   
   private void generateQuery()
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
            col.generateQuery(queryState, Expression.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
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
            queryState.queryString = "";
            from.generateFromString(queryState);
            query += queryState.queryString + " " + queryState.getFromAlias(from);
         }
      }
      if (where != null)
      {
         query += " WHERE ";
         queryState.queryString = "";
         where.generateQuery(queryState, Expression.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
         query += queryState.queryString;
      }
      queryString = query;
      queryParameters = queryState.parameters;
   }
   
   @Override
   public String getQueryString()
   {
      if (queryString == null)
         generateQuery();
      return queryString;
   }

   @Override
   public List<GeneratedQueryParameter> getQueryParameters()
   {
      if (queryParameters == null)
         generateQuery();
      return queryParameters;
   }
   
   @Override
   public RowReader<T> getRowReader()
   {
      return cols.reader;
   }
   
   public boolean isSelectFromWhere()
   {
      return !isAggregated;
   }

//   @Override
//   public JPQLQuery<T> copy()
//   {
//      SelectFromWhere<T> newQuery = new SelectFromWhere<>();
//      newQuery.query = query;
//      return newQuery;
//   }
}
