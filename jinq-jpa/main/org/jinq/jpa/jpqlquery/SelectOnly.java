package org.jinq.jpa.jpqlquery;

import java.util.List;

public class SelectOnly<T> extends JPQLQuery<T>
{
   public ColumnExpressions<T> cols;
   public boolean isAggregated = false;
   public boolean isDistinct = false;
   
   public SelectOnly() {}
   
   @Override
   public String getQueryString()
   {
      throw new IllegalArgumentException("SelectOnly should only be used internally and not for generating queries");
   }

   @Override
   public List<GeneratedQueryParameter> getQueryParameters()
   {
      throw new IllegalArgumentException("SelectOnly should only be used internally and not for generating queries");
   }
   
   @Override
   public RowReader<T> getRowReader()
   {
      return cols.reader;
   }
   
   @Override public boolean isSelectFromWhere()
   {
      return false;
   }
   
   @Override public boolean isSelectOnly()
   {
      return !isDistinct && !isAggregated;
   }

   @Override public boolean isSelectFromWhereGroupHaving()
   {
      return false;
   }

   @Override public boolean canSort()
   {
      return false;
   }
   
   @Override public boolean canDistinct()
   {
      return !isAggregated && !isDistinct;
   }
   
   @Override public boolean canAggregate()
   {
      return !isAggregated;
   }
   
   @Override public boolean canUnsortAggregate()
   {
      return !isAggregated;
   }
   
   @Override public boolean isValidSubquery()
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
