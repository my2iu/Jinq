package org.jinq.jooq;

import org.jinq.jooq.transform.MetamodelUtil;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.impl.TableImpl;

public class JinqJooqContext
{
   DSLContext dsl;
   MetamodelUtil metamodel;
   
   private JinqJooqContext(DSLContext context, Schema schema)
   {
      this.dsl = context;
      metamodel = new MetamodelUtil(schema);
   }
   
   public static JinqJooqContext using(DSLContext context, Schema schema)
   {
      return new JinqJooqContext(context, schema);
   }

   public <T extends Record> JinqJooqQuery<T> from(TableImpl<T> from)
   {
      return new JinqJooqQuery<>(this, from); 
   }
   public <T extends Record, U extends Record> 
   JinqJooqQuery2<T, U> from(TableImpl<T> from1, TableImpl<U> from2)
   {
      return new JinqJooqQuery2<>(this, from1, from2); 
   }
   public <T extends Record, U extends Record, V extends Record> 
   JinqJooqQuery3<T, U, V> from(TableImpl<T> from1, TableImpl<U> from2, TableImpl<V> from3)
   {
      return new JinqJooqQuery3<>(this, from1, from2, from3); 
   }
   public <T extends Record, U extends Record, V extends Record, W extends Record> 
   JinqJooqQuery4<T, U, V, W> from(TableImpl<T> from1, TableImpl<U> from2, TableImpl<V> from3, TableImpl<W> from4)
   {
      return new JinqJooqQuery4<>(this, from1, from2, from3, from4); 
   }
   public <T extends Record, U extends Record, V extends Record, W extends Record, X extends Record> 
   JinqJooqQuery5<T, U, V, W, X> from(TableImpl<T> from1, TableImpl<U> from2, TableImpl<V> from3, TableImpl<W> from4, TableImpl<X> from5)
   {
      return new JinqJooqQuery5<>(this, from1, from2, from3, from4, from5); 
   }
}
