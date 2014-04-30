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
   
   public int getNumColumns()
   {
      return columns.size();
   }
}
