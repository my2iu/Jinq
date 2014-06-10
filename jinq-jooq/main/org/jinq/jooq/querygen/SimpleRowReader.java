package org.jinq.jooq.querygen;

import org.jooq.Record;

public class SimpleRowReader<T> implements RowReader<T>
{
   @Override
   public T readResult(Record result)
   {
      return readResult(result, 0);
   }

   @SuppressWarnings("unchecked")
   @Override
   public T readResult(Record results, int offset)
   {
      return (T)results.getValue(offset);
   }

   @Override 
   public int getNumColumns()
   {
      return 1;
   }
}
