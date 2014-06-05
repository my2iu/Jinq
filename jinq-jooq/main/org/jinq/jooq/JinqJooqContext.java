package org.jinq.jooq;

import java.util.HashSet;
import java.util.Set;

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
}
