package org.jinq.jpa.jpqlquery;

public class SimpleRowReader<T> implements RowReader<T>
{
   @SuppressWarnings("unchecked")
   @Override
   public T readResult(Object result)
   {
      return (T)result;
   }

}
