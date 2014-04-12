package ch.epfl.labos.iu.orm.query2;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import ch.epfl.labos.iu.orm.Pair;
import ch.epfl.labos.iu.orm.Tuple3;
import ch.epfl.labos.iu.orm.Tuple4;
import ch.epfl.labos.iu.orm.Tuple5;
import ch.epfl.labos.iu.orm.Tuple8;

public abstract class SQLReader<T> 
{
   public abstract int getNumColumns();
   public abstract T readData(ResultSet result, int column) throws SQLException;
   public int getColumnForField(String field)
   {
      return -1;
   }
   public SQLReader getReaderForField(String field)
   {
      return null;
   }
   public int getColumnIndexForColumnName(String col)
   {
      return -1;
   }
   // TODO: Right now, this only checks downcasts, not upcasts
   public abstract boolean isCastConsistent(String internalName);
   
   public static class BooleanSQLReader extends SQLReader<Boolean>
   {
      public int getNumColumns()
      {
         return 1;
      }
      public Boolean readData(ResultSet result, int column) throws SQLException
      {
         return result.getBoolean(column);
      }
      public boolean isCastConsistent(String internalName)
      {
         return "java/lang/Boolean".equals(internalName);
      }

   }
   public static class IntegerSQLReader extends SQLReader<Integer>
   {
      public int getNumColumns()
      {
         return 1;
      }
      public Integer readData(ResultSet result, int column) throws SQLException
      {
         return result.getInt(column);
      }
      public boolean isCastConsistent(String internalName)
      {
         return "java/lang/Integer".equals(internalName);
      }
   }
   public static class FloatSQLReader extends SQLReader<Float>
   {
      public int getNumColumns()
      {
         return 1;
      }
      public Float readData(ResultSet result, int column) throws SQLException
      {
         return result.getFloat(column);
      }
      public boolean isCastConsistent(String internalName)
      {
         return "java/lang/Float".equals(internalName);
      }
   }
   public static class DoubleSQLReader extends SQLReader<Double>
   {
      public int getNumColumns()
      {
         return 1;
      }
      public Double readData(ResultSet result, int column) throws SQLException
      {
         return result.getDouble(column);
      }
      public boolean isCastConsistent(String internalName)
      {
         return "java/lang/Double".equals(internalName);
      }
   }
   public static class StringSQLReader extends SQLReader<String>
   {
      public int getNumColumns()
      {
         return 1;
      }
      public String readData(ResultSet result, int column) throws SQLException
      {
         return result.getString(column);
      }
      public boolean isCastConsistent(String internalName)
      {
         return "java/lang/String".equals(internalName);
      }
   }
   public static class DateSQLReader extends SQLReader<Date>
   {
      public int getNumColumns()
      {
         return 1;
      }
      public Date readData(ResultSet result, int column) throws SQLException
      {
         return result.getDate(column);
      }
      public boolean isCastConsistent(String internalName)
      {
         return "java/sql/Date".equals(internalName);
      }
   }
   public static class ArrayTupleSQLReader extends SQLReader<Object[]>
   {
      SQLReader[] subreaders;
      
      public ArrayTupleSQLReader(SQLReader[] subreaders)
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

      @Override public Object[] readData(ResultSet result, int column) throws SQLException
      {
         Object []data = new Object[subreaders.length];
         int offset = 0;
         for (int n = 0; n < subreaders.length; n++)
         {
            data[n] = subreaders[n].readData(result, column + offset);
            offset += subreaders[n].getNumColumns();
         }
         return data;
      }

      @Override public boolean isCastConsistent(String internalName)
      {
         return false;
      }
   }
   public static class TupleSQLReader<U> extends SQLReader<U>
   {
      SQLReader[] subreaders;
      
      TupleSQLReader(SQLReader[] subreaders)
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

      int getFieldIndex(String field)
      {
         if ("One".equals(field)) 
            return 0;
         else if ("Two".equals(field)) 
            return 1;
         else if ("Three".equals(field)) 
            return 2;
         else if ("Four".equals(field)) 
            return 3;
         else if ("Five".equals(field)) 
            return 4;
         else if ("Six".equals(field)) 
            return 5;
         else if ("Seven".equals(field)) 
            return 6;
         else if ("Eight".equals(field)) 
            return 7;
         else 
            return -1;
      }
      
      @Override public int getColumnForField(String field)
      {
         int index = getFieldIndex(field); 
         if (index < 0 || index >= subreaders.length) return -1;
         int offset = 0;
         for (int n = 0; n < index; n++)
            offset += subreaders[n].getNumColumns();
         return offset;
      }
      
      @Override public SQLReader getReaderForField(String field)
      {
         int index = getFieldIndex(field); 
         if (index < 0 || index >= subreaders.length) return null;
         return subreaders[index];
      }

      U createTuple(Object[]data)
      {
         switch(subreaders.length)
         {
            case 2:
               return (U)new Pair(data[0], data[1]);
            case 3:
               return (U)new Tuple3(data[0], data[1], data[2]);
            case 4:
               return (U)new Tuple4(data[0], data[1], data[2], data[3]);
            case 5:
               return (U)new Tuple5(data[0], data[1], data[2], data[3], data[4]);
            case 8:
               return (U)new Tuple8(data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7]);
            default:
               throw new IllegalArgumentException("Creating a tuple with a SQLReader with unknown size " + subreaders.length);
         }
      }
      @Override public U readData(ResultSet result, int column) throws SQLException
      {
         Object []data = new Object[subreaders.length];
         int offset = 0;
         for (int n = 0; n < subreaders.length; n++)
         {
            data[n] = subreaders[n].readData(result, column + offset);
            offset += subreaders[n].getNumColumns();
         }
         return createTuple(data);
      }
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
      public static <T> TupleSQLReader<T> createReaderForTuple(String tupleInternalName, SQLReader...subreaders)
      {
         if ("ch/epfl/labos/iu/orm/Pair".equals(tupleInternalName))
            return new PairSQLReader(subreaders[0], subreaders[1]);
         else if ("ch/epfl/labos/iu/orm/Tuple3".equals(tupleInternalName))
            return new Tuple3SQLReader(subreaders[0], subreaders[1], subreaders[2]);
         else if ("ch/epfl/labos/iu/orm/Tuple4".equals(tupleInternalName))
            return new Tuple4SQLReader(subreaders[0], subreaders[1], subreaders[2], subreaders[3]);
         else if ("ch/epfl/labos/iu/orm/Tuple5".equals(tupleInternalName))
            return new Tuple5SQLReader(subreaders[0], subreaders[1], subreaders[2], subreaders[3], subreaders[4]);
         else if ("ch/epfl/labos/iu/orm/Tuple8".equals(tupleInternalName))
            return new Tuple8SQLReader(subreaders[0], subreaders[1], subreaders[2], subreaders[3], subreaders[4], subreaders[5], subreaders[6], subreaders[7]);
         else
            throw new IllegalArgumentException("Creating a tuple with a SQLReader with unknown size " + subreaders.length);
      }
   }
   public static class PairSQLReader<U,V> extends TupleSQLReader<Pair<U,V>>
   {
      public PairSQLReader(SQLReader<U> val1, SQLReader<V> val2)
      {
         super(new SQLReader[]{val1, val2});
      }
   }
   public static class Tuple3SQLReader<U1,U2,U3> extends TupleSQLReader<Tuple3<U1,U2,U3>>
   {
      public Tuple3SQLReader(SQLReader<U1> val1, SQLReader<U2> val2, SQLReader<U3> val3)
      {
         super(new SQLReader[]{val1, val2, val3});
      }
   }
   public static class Tuple4SQLReader<U1,U2,U3,U4> extends TupleSQLReader<Tuple4<U1,U2,U3,U4>>
   {
      public Tuple4SQLReader(SQLReader<U1> val1, SQLReader<U2> val2, SQLReader<U3> val3, SQLReader<U4> val4)
      {
         super(new SQLReader[]{val1, val2, val3, val4});
      }
   }
   public static class Tuple5SQLReader<U1,U2,U3,U4,U5> extends TupleSQLReader<Tuple5<U1,U2,U3,U4,U5>>
   {
      public Tuple5SQLReader(SQLReader<U1> val1, SQLReader<U2> val2, SQLReader<U3> val3, SQLReader<U4> val4, SQLReader<U5> val5)
      {
         super(new SQLReader[]{val1, val2, val3, val4, val5});
      }
   }
   public static class Tuple8SQLReader<U1,U2,U3,U4,U5,U6,U7,U8> extends TupleSQLReader<Tuple8<U1,U2,U3,U4,U5,U6,U7,U8>>
   {
      public Tuple8SQLReader(SQLReader<U1> val1, SQLReader<U2> val2, SQLReader<U3> val3, SQLReader<U4> val4, SQLReader<U5> val5, SQLReader<U6> val6, SQLReader<U7> val7, SQLReader<U8> val8)
      {
         super(new SQLReader[]{val1, val2, val3, val4, val5, val6, val7, val8});
      }
   }
}
