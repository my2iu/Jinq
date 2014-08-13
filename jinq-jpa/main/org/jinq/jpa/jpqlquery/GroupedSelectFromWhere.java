package org.jinq.jpa.jpqlquery;

public class GroupedSelectFromWhere<T, U> extends SelectFromWhere<T>
{
   public ColumnExpressions<U> groupingCols;

   protected String generateQueryContents(QueryGenerationState queryState)
   {
      String query = generateSelectFromWhere(queryState);
      query += generateGroupBy(queryState);
      query += generateSort(queryState);
      return query;
   }
   
   protected String generateGroupBy(QueryGenerationState queryState)
   {
      String query = "";
      query += " GROUP BY ";
      boolean isFirst = true;
      for (Expression col: groupingCols.columns)
      {
         if (!isFirst) query += ", ";
         isFirst = false;
         queryState.queryString = "";
         col.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
         query += queryState.queryString;
      }
      return query;
   }
   
   public boolean isSelectFromWhere()
   {
      return false;
   }
   
   public GroupedSelectFromWhere<T, U> shallowCopy()
   {
      GroupedSelectFromWhere<T, U> copy = new GroupedSelectFromWhere<>();
      copySelectFromWhereTo(copy);
      copy.groupingCols = groupingCols;
      return copy;
   }
}
