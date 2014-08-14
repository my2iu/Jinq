package org.jinq.jpa.jpqlquery;

import java.util.ArrayList;
import java.util.List;

public class SelectFromWhere<T> extends SelectOnly<T>
{
   public List<From> froms = new ArrayList<>();
   public Expression where;
   public List<SortingParameters> sort = new ArrayList<>();
   public long limit = -1;
   public long skip = -1;
   
   /**
    * Holds information needed to do a sort. 
    */
   public static class SortingParameters
   {
      public Expression expr;
      public boolean isAscending;
   }
   
   /**
    * After a JPQL query is generated, this stores the resulting query string.  
    */
   private String queryString;
   /**
    * After a JPQL query is generated, this stores parameters that need to be 
    * filled-in, in the query  
    */
   private List<GeneratedQueryParameter> queryParameters;
   
   protected void generateQuery()
   {
      QueryGenerationState queryState = new QueryGenerationState();

      prepareQueryGeneration(queryState);
      
      // Now generate the query
      queryString = generateQueryContents(queryState);
      queryParameters = queryState.parameters;
   }
   
   protected void prepareQueryGeneration(QueryGenerationState queryState)
   {
      // Generate aliases for each of the FROM entries
      for (From from: froms)
      {
         queryState.generateFromAlias(from);
      }
   }
   
   protected String generateQueryContents(QueryGenerationState queryState)
   {
      String query = generateSelectFromWhere(queryState);
      query += generateSort(queryState);
      return query;
   }

   protected String generateSelectFromWhere(QueryGenerationState queryState)
   {
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
            col.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
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
         where.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
         query += queryState.queryString;
      }
      return query;
   }
   
   protected String generateSort(QueryGenerationState queryState)
   {
      String query = "";
      if (!sort.isEmpty())
      {
         query += " ORDER BY";
         boolean isFirst = true;
         for (SortingParameters sortParams: sort)
         {
            if (!isFirst) query += ",";
            isFirst = false;
            queryState.queryString = " ";
            sortParams.expr.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
            query += queryState.queryString;
            query += (sortParams.isAscending ? " ASC" : " DESC");
         }
      }
      return query;
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
      return !isAggregated && sort.isEmpty() && limit < 0 && skip < 0;
   }

   public boolean canSort()
   {
      return ((this.getClass() == SelectFromWhere.class && !isAggregated) || (this instanceof GroupedSelectFromWhere))
            && limit < 0 && skip < 0;
   }
   
   public <U> GroupedSelectFromWhere<T, U> shallowCopyWithGrouping()
   {
      GroupedSelectFromWhere<T, U> copy = new GroupedSelectFromWhere<>();
      copySelectFromWhereTo(copy);
      return copy;
   }

   protected void copySelectFromWhereTo(SelectFromWhere<T> copy)
   {
      copy.cols = cols;
      copy.froms.addAll(froms);
      copy.where = where;
      copy.isAggregated = isAggregated;
      copy.sort.addAll(sort);
      copy.limit = limit;
      copy.skip = skip;
   }
   
   public SelectFromWhere<T> shallowCopy()
   {
      SelectFromWhere<T> copy = new SelectFromWhere<>();
      copySelectFromWhereTo(copy);
      return copy;
   }
   
//   @Override
//   public JPQLQuery<T> copy()
//   {
//      SelectFromWhere<T> newQuery = new SelectFromWhere<>();
//      newQuery.query = query;
//      return newQuery;
//   }
}
