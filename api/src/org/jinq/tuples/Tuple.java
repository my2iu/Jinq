package org.jinq.tuples;

public class Tuple
{
   public static <T extends Tuple> T createTuple(Object[] data)
   {
      switch(data.length)
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
            throw new IllegalArgumentException("Creating a tuple with unknown size " + data.length);
      }
   }
}
