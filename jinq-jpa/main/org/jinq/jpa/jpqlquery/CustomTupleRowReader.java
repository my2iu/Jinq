package org.jinq.jpa.jpqlquery;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Used to read data into a custom tuple
 */
public class CustomTupleRowReader<T> implements RowReader<T>
{
   Method staticBuilder;
   RowReader<?>[] subreaders;
   
   public CustomTupleRowReader(Method staticBuilder, RowReader<?>[] subreaders)
   {
      this.staticBuilder = staticBuilder;
      this.subreaders = subreaders;
   }

   @Override public int getNumColumns()
   {
      int sum = 0;
      for (int n = 0; n < subreaders.length; n++)
         sum += subreaders[n].getNumColumns();
      return sum;
   }
   
   @Override
   public T readResult(Object result)
   {
      if (!(result instanceof Object[]))
         throw new IllegalArgumentException("Expecting an array of results");
      return readResult((Object[])result, 0);
   }

   @Override
   public T readResult(Object[] results, int start)
   {
      Object [] data = new Object[subreaders.length];
      int offset = 0;
      for (int n = 0; n < subreaders.length; n++)
      {
         data[n] = subreaders[n].readResult(results, start + offset);
         offset += subreaders[n].getNumColumns();
      }
      try {
         return (T)staticBuilder.invoke(null, data);
      } catch (IllegalAccessException | InvocationTargetException e)
      {
         throw new IllegalArgumentException("Cannot invoke builder method for custom tuple", e);
      }
   }
   

   public int getColumnForIndex(int index)
   {
      if (index < 0 || index >= subreaders.length) return -1;
      int offset = 0;
      for (int n = 0; n < index; n++)
         offset += subreaders[n].getNumColumns();
      return offset;
   }

   public RowReader<?> getReaderForIndex(int index)
   {
      if (index < 0 || index >= subreaders.length) return null;
      return subreaders[index];
   }
}
