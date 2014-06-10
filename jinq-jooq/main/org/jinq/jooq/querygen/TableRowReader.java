package org.jinq.jooq.querygen;

import java.lang.reflect.Constructor;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

public class TableRowReader<T extends Record> implements RowReader<T>
{
   private Constructor<? extends T> constructor;
   private Table<T> table;
   
   public TableRowReader(Table<T> table)
   {
      this.table = table;
      try {
         constructor = table.getRecordType().getDeclaredConstructor();
      } catch (Exception e) {
         throw new IllegalArgumentException("Cannot find constructor for class " + table.getRecordType().getName());
      }
   }

   @Override
   public T readResult(Record record)
   {
      return readResult(record, 0);
   }

   @Override
   public T readResult(Record record, int offset)
   {
      T toReturn;
      try {
         toReturn = constructor.newInstance();
      } catch (Exception e) {
         throw new IllegalArgumentException("Cannot construct class " + table.getRecordType().getName());
      }
      Field<?>[] fields = table.fields(); 
      for (int idx = 0; idx < fields.length; idx++)
      {
         Field<?> f = fields[idx];
         copyValueIntoRecord(toReturn, record, f, idx);
      }
      return toReturn;
   }
   
   private <K> void copyValueIntoRecord(T outputRecord, Record inputRecord, Field<K> field, int idx)
   {
      outputRecord.setValue(field, inputRecord.getValue(idx, field.getConverter()));
   }

   @Override
   public int getNumColumns()
   {
      return table.fields().length;
   }

   public <U> RowReader<U> getReaderForField(Field<?> field)
   {
      Field<?>[] fields = table.fields(); 
      for (int idx = 0; idx < fields.length; idx++)
      {
         Field<?> f = fields[idx];
         if (f == field)
            return new SimpleRowReader<>();
      }
      throw new IllegalArgumentException("Unknown field");
   }
   
   public int getIndexForField(Field<?> field)
   {
      Field<?>[] fields = table.fields();
      int colIndex = 0;
      for (int idx = 0; idx < fields.length; idx++)
      {
         Field<?> f = fields[idx];
         if (f == field)
            return colIndex;
         colIndex += getReaderForField(field).getNumColumns();
      }
      throw new IllegalArgumentException("Unknown field");
   }
}
