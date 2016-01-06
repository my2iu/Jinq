package org.jinq.jpa.jpqlquery;

import java.util.List;

public class SelectOnly<T> extends JPQLQuery<T>
{
   public ColumnExpressions<T> cols;
   public boolean isAggregated = false;
   public boolean isDistinct = false;
   
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
   
   public boolean canAggregate()
   {
      return !isAggregated;
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
