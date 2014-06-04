package org.jinq.jooq.querygen;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.TableImpl;

public class TableRowReader<T extends Record> implements RowReader<T>
{
   private Constructor<? extends T> constructor;
   private TableImpl<T> table;
   
   public TableRowReader(TableImpl<T> table)
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

}
