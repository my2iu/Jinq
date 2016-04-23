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
      prepareQueryGeneration(Expression.QueryGenerationPreparationPhase.FROM, queryState);
   }
   
   protected void prepareQueryGeneration(Expression.QueryGenerationPreparationPhase preparePhase,
         QueryGenerationState queryState)
   {
      for (Expression col: cols.columns)
      {
         col.prepareQueryGeneration(preparePhase, queryState);
      }      
      for (From from: froms)
      {
         from.prepareQueryGeneration(preparePhase, queryState);
      }
      if (where != null)
         where.prepareQueryGeneration(preparePhase, queryState);
      for (SortingParameters sortParams: sort)
      {
         sortParams.expr.prepareQueryGeneration(preparePhase, queryState);
      }
   }
   
   protected String generateQueryContents(QueryGenerationState queryState)
   {
      generateSelectFromWhere(queryState);
      generateSort(queryState);
      return queryState.queryString;
   }

   protected void generateSelectFromWhere(QueryGenerationState queryState)
   {
      if (cols.getNumColumns() > 0)
      {
         queryState.queryString += "SELECT ";
         if (isDistinct)
            queryState.queryString += "DISTINCT ";
         boolean isFirst = true;
         for (Expression col: cols.columns)
         {
            if (!isFirst) queryState.queryString += ", ";
            isFirst = false;
            col.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
         }
      }
      if (froms.size() > 0)
      {
         queryState.queryString += " FROM ";
         boolean isFirst = true;
         for (From from: froms)
         {
            if (!isFirst)
            {
               if (from.isPrecededByComma())
                  queryState.queryString += ", ";
            }
            from.generateFromString(queryState, queryState.getFromAlias(from), isFirst);
            isFirst = false;
         }
      }
      if (where != null)
      {
         queryState.queryString += " WHERE ";
         where.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
      }
   }
   
   protected void generateSort(QueryGenerationState queryState)
   {
      if (!sort.isEmpty())
      {
         queryState.queryString += " ORDER BY";
         boolean isFirst = true;
         for (SortingParameters sortParams: sort)
         {
            if (!isFirst) queryState.queryString += ",";
            isFirst = false;
            queryState.queryString += " ";
            if (sortParams.expr instanceof SubqueryExpression)
               // Special handling of subquery parantheses
               queryState.queryString += "(";
            sortParams.expr.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_ORDER_BY_UNRESTRICTED_OPERATOR_PRECEDENCE);
            queryState.queryString += (sortParams.isAscending ? " ASC" : " DESC");
            if (sortParams.expr instanceof SubqueryExpression)
               // Special handling of subquery parantheses
               queryState.queryString += ")";
         }
      }
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
      return !isAggregated && sort.isEmpty() && limit < 0 && skip < 0 && !isDistinct;
   }
   
   public boolean isSelectOnly()
   {
      return false;
   }

   public boolean canSort()
   {
      return ((this.getClass() == SelectFromWhere.class && !isAggregated) || (this instanceof GroupedSelectFromWhere))
            && limit < 0 && skip < 0;
   }
   
   public boolean canDistinct()
   {
      return isSelectFromWhere();
   }
   
   public boolean canAggregate()
   {
      return !isAggregated && sort.isEmpty() && limit < 0 && skip < 0;
   }
   
   public boolean isValidSubquery()
   {
      return limit < 0 && skip < 0 && sort.isEmpty();
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
      copy.isDistinct = isDistinct;
   }
   
   @Override
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
