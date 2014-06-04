package org.jinq.jooq;

import static org.jinq.jooq.test.generated.Tables.CUSTOMERS;

import java.util.List;

import org.jinq.jooq.querygen.RowReader;
import org.jinq.jooq.querygen.TableRowReader;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.TableImpl;

public class JinqJooqQuery<T extends Record>
{
   DSLContext context;
   TableImpl<T> from;
   RowReader<T> reader;
   
   public JinqJooqQuery(DSLContext context, TableImpl<T> from)
   {
      this.context = context;
      this.from = from;
      this.reader = new TableRowReader<>(from);
   }
   
   public ResultStream<T> selectAll()
   {
      // TODO: Decide between defaulting to using cursors or defaulting
      // to using lists.
      List<Record> result = context.select().from(from).fetch();
      return new ResultStream<>(result.stream().map(r -> reader.readResult(r)));
   }
}
