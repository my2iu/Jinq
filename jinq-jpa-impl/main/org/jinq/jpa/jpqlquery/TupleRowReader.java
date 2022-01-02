package org.jinq.jpa.jpqlquery;

import org.jinq.tuples.Pair;
import org.jinq.tuples.Tuple3;
import org.jinq.tuples.Tuple4;
import org.jinq.tuples.Tuple5;
import org.jinq.tuples.Tuple6;
import org.jinq.tuples.Tuple7;
import org.jinq.tuples.Tuple8;

public class TupleRowReader<T> implements RowReader<T>
{
   public static final String PAIR_CLASS = Pair.class.getTypeName().replace('.', '/'); 
   public static final String TUPLE3_CLASS = Tuple3.class.getTypeName().replace('.', '/'); 
   public static final String TUPLE4_CLASS = Tuple4.class.getTypeName().replace('.', '/');
   public static final String TUPLE5_CLASS = Tuple5.class.getTypeName().replace('.', '/');
   public static final String TUPLE6_CLASS = Tuple6.class.getTypeName().replace('.', '/');
   public static final String TUPLE7_CLASS = Tuple7.class.getTypeName().replace('.', '/');
   public static final String TUPLE8_CLASS = Tuple8.class.getTypeName().replace('.', '/');
   
   RowReader<?>[] subreaders;
   
   TupleRowReader(RowReader<?>[] subreaders)
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
   
//   private int getFieldIndex(String field)
//   {
//      if ("One".equals(field)) 
//         return 0;
//      else if ("Two".equals(field)) 
//         return 1;
//      else if ("Three".equals(field)) 
//         return 2;
//      else if ("Four".equals(field)) 
//         return 3;
//      else if ("Five".equals(field)) 
//         return 4;
//      else if ("Six".equals(field)) 
//         return 5;
//      else if ("Seven".equals(field)) 
//         return 6;
//      else if ("Eight".equals(field)) 
//         return 7;
//      else 
//         return -1;
//   }

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
            return (T)new Pair(data[0], data[1]);
         case 3:
            return (T)new Tuple3(data[0], data[1], data[2]);
         case 4:
            return (T)new Tuple4(data[0], data[1], data[2], data[3]);
         case 5:
            return (T)new Tuple5(data[0], data[1], data[2], data[3], data[4]);
         case 6:
            return (T)new Tuple6(data[0], data[1], data[2], data[3], data[4], data[5]);
         case 7:
            return (T)new Tuple7(data[0], data[1], data[2], data[3], data[4], data[5], data[6]);
         case 8:
            return (T)new Tuple8(data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7]);
         default:
            throw new IllegalArgumentException("Creating a tuple with a SQLReader with unknown size " + subreaders.length);
      }
   }

/*
      public String getTupleInternalName()
      {
         switch(subreaders.length)
         {
            case 2: return "ch/epfl/labos/iu/orm/Pair"; 
            case 3: return "ch/epfl/labos/iu/orm/Tuple3"; 
            case 4: return "ch/epfl/labos/iu/orm/Tuple4"; 
            case 5: return "ch/epfl/labos/iu/orm/Tuple5"; 
            case 8: return "ch/epfl/labos/iu/orm/Tuple8"; 
            default:
               throw new IllegalArgumentException("Creating a tuple with a SQLReader with unknown size " + subreaders.length);
         }
      }
      public boolean isCastConsistent(String internalName)
      {
         String tupleName = getTupleInternalName();
         return tupleName.equals(internalName);
      }

   
 */
   
   public static <T> TupleRowReader<T> createReaderForTuple(RowReader<?>...subreaders)
   {
      return new TupleRowReader<T>(subreaders);
   }

   public static <T> TupleRowReader<T> createReaderForTuple(String tupleInternalName, RowReader<?>...subreaders)
   {
      if (PAIR_CLASS.equals(tupleInternalName)) {
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
      else if (TUPLE6_CLASS.equals(tupleInternalName))
      {
         if (subreaders.length != 6)
            throw new IllegalArgumentException("Wrong number of arguments when creating tuple");
      }
      else if (TUPLE7_CLASS.equals(tupleInternalName))
      {
         if (subreaders.length != 7)
            throw new IllegalArgumentException("Wrong number of arguments when creating tuple");
      }
      else if (TUPLE8_CLASS.equals(tupleInternalName))
      {
         if (subreaders.length != 8)
            throw new IllegalArgumentException("Wrong number of arguments when creating tuple");
      }
      else
         throw new IllegalArgumentException("Creating a tuple with a SQLReader with unknown size " + subreaders.length);
      return new TupleRowReader<T>(subreaders);
   }
   
}
