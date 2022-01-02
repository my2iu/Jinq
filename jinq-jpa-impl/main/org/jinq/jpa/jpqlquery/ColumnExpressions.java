package org.jinq.jpa.jpqlquery;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the expressions that make up the various columns of the query and the
 * reader used to interpret the returned result as an object.
 *
 */
public class ColumnExpressions<T>
{
   public List<Expression> columns = new ArrayList<>();
   public RowReader<T> reader;
   
   public ColumnExpressions(RowReader<T> reader)
   {
      this.reader = reader;
   }
   
   public static <U> ColumnExpressions<U> singleColumn(RowReader<U> reader, Expression expr)
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
   
   public Expression getOnlyColumn()
   {
      if (!isSingleColumn()) throw new IllegalArgumentException("Expecting a single column");
      return columns.get(0);
   }
}
