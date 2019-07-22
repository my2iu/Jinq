package org.jinq.jpa.jpqlquery;

import java.util.List;

public class ParameterAsQuery<T> extends JPQLQuery<T>
{
   public ColumnExpressions<T> cols;
   
   @Override
   public String getQueryString()
   {
      throw new IllegalArgumentException("ParameterAsQuery should only be used internally and not for generating queries");
   }

   @Override
   public List<GeneratedQueryParameter> getQueryParameters()
   {
      throw new IllegalArgumentException("ParameterAsQuery should only be used internally and not for generating queries");
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
      return false;
   }

   @Override public boolean isSelectFromWhereGroupHaving()
   {
      return false;
   }
   
   @Override public boolean canSelectWhere()
   {
      return false;
   }

   @Override public boolean canSelectHaving()
   {
      return false;
   }
   
   @Override public boolean canSort()
   {
      return false;
   }
   
   @Override public boolean canDistinct()
   {
      return false;
   }

   @Override public boolean canAggregate()
   {
      return false;
   }

   @Override public boolean canUnsortAggregate()
   {
      return false;
   }

   @Override public boolean isValidSubquery()
   {
      return true;
   }

   public ParameterAsQuery<T> shallowCopy()
   {
      ParameterAsQuery<T> copy = new ParameterAsQuery<>();
      copy.cols = cols;
      return copy;
   }
   
}
