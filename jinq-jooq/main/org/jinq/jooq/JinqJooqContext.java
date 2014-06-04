package org.jinq.jooq;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.TableImpl;

public class JinqJooqContext
{
   DSLContext context;
   
   public JinqJooqContext(DSLContext context)
   {
      this.context = context;
   }
   
   public <T extends Record> JinqJooqQuery<T> from(TableImpl<T> from)
   {
      return new JinqJooqQuery<>(context, from); 
   }
}
