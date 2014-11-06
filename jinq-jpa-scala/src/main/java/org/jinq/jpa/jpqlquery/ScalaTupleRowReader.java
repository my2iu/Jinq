package org.jinq.jpa.jpqlquery;

import scala.Tuple2;
import scala.Tuple3;
import scala.Tuple4;
import scala.Tuple5;
import scala.Tuple8;


public class ScalaTupleRowReader<T> implements RowReader<T>
{
   public static final String TUPLE2_CLASS = Tuple2.class.getTypeName().replace('.', '/'); 
   public static final String TUPLE3_CLASS = Tuple3.class.getTypeName().replace('.', '/'); 
   public static final String TUPLE4_CLASS = Tuple4.class.getTypeName().replace('.', '/');
   public static final String TUPLE5_CLASS = Tuple5.class.getTypeName().replace('.', '/');
   public static final String TUPLE8_CLASS = Tuple8.class.getTypeName().replace('.', '/');
   
   RowReader<?>[] subreaders;
   
   ScalaTupleRowReader(RowReader<?>[] subreaders)
   {
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
      return createTuple(data);
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

   private T createTuple(Object[] data)
   {
      switch(subreaders.length)
      {
         case 2:
            return (T)new Tuple2(data[0], data[1]);
         case 3:
            return (T)new Tuple3(data[0], data[1], data[2]);
         case 4:
            return (T)new Tuple4(data[0], data[1], data[2], data[3]);
         case 5:
            return (T)new Tuple5(data[0], data[1], data[2], data[3], data[4]);
         case 8:
            return (T)new Tuple8(data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7]);
         default:
            throw new IllegalArgumentException("Creating a tuple with a SQLReader with unknown size " + subreaders.length);
      }
   }

   public static <T> ScalaTupleRowReader<T> createReaderForTuple(RowReader<?>...subreaders)
   {
      return new ScalaTupleRowReader<T>(subreaders);
   }

   public static <T> ScalaTupleRowReader<T> createReaderForTuple(String tupleInternalName, RowReader<?>...subreaders)
   {
      if (TUPLE2_CLASS.equals(tupleInternalName)) {
         if (subreaders.length != 2)
            throw new IllegalArgumentException("Wrong number of arguments when creating tuple");
      }
      else if (TUPLE3_CLASS.equals(tupleInternalName))
      {
         if (subreaders.length != 3)
            throw new IllegalArgumentException("Wrong number of arguments when creating tuple");
      }
      else if (TUPLE4_CLASS.equals(tupleInternalName))
      {
         if (subreaders.length != 4)
            throw new IllegalArgumentException("Wrong number of arguments when creating tuple");
      }
      else if (TUPLE5_CLASS.equals(tupleInternalName))
      {
         if (subreaders.length != 5)
            throw new IllegalArgumentException("Wrong number of arguments when creating tuple");
      }
      else if (TUPLE8_CLASS.equals(tupleInternalName))
      {
         if (subreaders.length != 8)
            throw new IllegalArgumentException("Wrong number of arguments when creating tuple");
      }
      else
         throw new IllegalArgumentException("Creating a tuple with a SQLReader with unknown size " + subreaders.length);
      return new ScalaTupleRowReader<T>(subreaders);
   }
   
}
