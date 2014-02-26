package ch.epfl.labos.iu.orm.queryll2;

import ch.epfl.labos.iu.orm.query2.SQLFragment;
import ch.epfl.labos.iu.orm.query2.SQLReader;
import ch.epfl.labos.iu.orm.query2.SQLSubstitution;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class SQLColumnValues<T>
{
   // Unlike a SQLFragment which holds the value of one column,
   // this holds the value of multiple columns together with a
   // reader. This class is used inside TypedValueVisitor instances
   // when generating SQL from a symbolic execution expression tree.
   SQLReader<T> reader;
   SQLFragment [] columns;

   public SQLColumnValues(SQLReader<T> reader)
   {
      this.reader = reader;
      columns = new SQLFragment[reader.getNumColumns()];
      for (int n = 0; n < columns.length; n++)
         columns[n] = new SQLFragment();
   }
   
   public SQLFragment getColumn(int column)
   {
      return columns[column];
   }
   
   public int getNumColumns()
   {
      return reader.getNumColumns();
   }

   // These are only valid if there is only one column (is this correct?)
   public SQLColumnValues<T> add(String str) throws TypedValueVisitorException
   {
      if (columns.length != 1) 
         throw new TypedValueVisitorException("Adding string to multi-column value");
      columns[0].add(str);
      return this;
   }
   public SQLColumnValues<T> add(SQLColumnValues toAdd) throws TypedValueVisitorException
   {
      if (columns.length != 1) 
         throw new TypedValueVisitorException("Adding to a multi-column value");
      if (columns.length != toAdd.columns.length)
         throw new TypedValueVisitorException("Adding together different number of columns");
      columns[0].add(toAdd.columns[0]);
      return this;
   }
}
