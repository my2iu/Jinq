package org.jinq.jpa.jpqlquery;

import java.util.List;

public class SelectOnly<T> extends JPQLQuery<T>
{
   public ColumnExpressions<T> cols;
   public boolean isAggregated = false;
   public boolean isDistinct = false;
   
//   /**
//    * After a JPQL query is generated, this stores the resulting query string.  
//    */
//   private String queryString;
   /**
    * After a JPQL query is generated, this stores parameters that need to be 
    * filled-in, in the query  
    */
//   private List<GeneratedQueryParameter> queryParameters;
   
//   private void generateQuery()
//   {
//      QueryGenerationState queryState = new QueryGenerationState();
//      
//      // Generate aliases for each of the FROM entries
//      for (From from: froms)
//      {
//         queryState.generateFromAlias(from);
//      }
//      
//      // Now generate the query
//      String query = "";
//      if (cols.getNumColumns() > 0)
//      {
//         query += "SELECT ";
//         boolean isFirst = true;
//         for (Expression col: cols.columns)
//         {
//            if (!isFirst) query += ", ";
//            isFirst = false;
//            queryState.queryString = "";
//            col.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
//            query += queryState.queryString;
//         }
//      }
//      if (froms.size() > 0)
//      {
//         query += " FROM ";
//         boolean isFirst = true;
//         for (From from: froms)
//         {
//            if (!isFirst) query += ", ";
//            isFirst = false;
//            queryState.queryString = "";
//            from.generateFromString(queryState);
//            query += queryState.queryString + " " + queryState.getFromAlias(from);
//         }
//      }
//      if (where != null)
//      {
//         query += " WHERE ";
//         queryState.queryString = "";
//         where.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
//         query += queryState.queryString;
//      }
//      if (!sort.isEmpty())
//      {
//         query += " ORDER BY";
//         boolean isFirst = true;
//         for (SortingParameters sortParams: sort)
//         {
//            if (!isFirst) query += ",";
//            isFirst = false;
//            queryState.queryString = " ";
//            sortParams.expr.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
//            query += queryState.queryString;
//            query += (sortParams.isAscending ? " ASC" : " DESC");
//         }
//      }
//      queryString = query;
//      queryParameters = queryState.parameters;
//   }
   
   @Override
   public String getQueryString()
   {
      throw new IllegalArgumentException("SelectOnly should only be used internally and not for generating queries");
//      if (queryString == null)
//         generateQuery();
//      return queryString;
   }

   @Override
   public List<GeneratedQueryParameter> getQueryParameters()
   {
      throw new IllegalArgumentException("SelectOnly should only be used internally and not for generating queries");
//      if (queryParameters == null)
//         generateQuery();
//      return queryParameters;
   }
   
   @Override
   public RowReader<T> getRowReader()
   {
      return cols.reader;
   }
   
   public boolean isSelectFromWhere()
   {
      return false;
   }
   
   public boolean isSelectOnly()
   {
      return !isDistinct && !isAggregated;
   }

   public boolean isSelectFromWhereGroupHaving()
   {
      return false;
   }

   public boolean canSort()
   {
      return false;
   }
   
   public boolean canDistinct()
   {
      return !isAggregated && !isDistinct;
   }
   
   public boolean isValidSubquery()
   {
      return false;
   }

   public SelectOnly<T> shallowCopy()
   {
      SelectOnly<T> copy = new SelectOnly<>();
      copy.cols = cols;
      copy.isAggregated = isAggregated;
      return copy;
   }
   
}
