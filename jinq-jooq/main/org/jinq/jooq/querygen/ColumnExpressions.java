package org.jinq.jooq.querygen;

import java.util.ArrayList;
import java.util.List;

import org.jooq.QueryPart;

/**
 * Holds the expressions that make up the various columns of the query and the
 * reader used to interpret the returned result as an object.
 *
 */
public class ColumnExpressions<T>
{
   public List<QueryPart> columns = new ArrayList<>();
   public RowReader<T> reader;
   
   public ColumnExpressions(RowReader<T> reader)
   {
      this.reader = reader;
   }
   
   public static <U> ColumnExpressions<U> singleColumn(RowReader<U> reader, QueryPart expr)
   {
      ColumnExpressions<U> columnExpressions = new ColumnExpressions<>(reader);
      columnExpressions.columns.add(expr);
      return columnExpressions;
   }
   
   public boolean isSingleColumn()
   {
      return getNumColumns() == 1;
   }
   
   public int getNumColumns()
   {
      return columns.size();
   }
   
   public QueryPart getOnlyColumn()
   {
      if (!isSingleColumn()) throw new IllegalArgumentException("Expecting a single column");
      return columns.get(0);
   }
}
