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
   
   public boolean isSelectFromWhere()
   {
      return false;
   }
   
   public boolean isSelectOnly()
   {
      return false;
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
      return false;
   }
   
   public boolean isValidSubquery()
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
