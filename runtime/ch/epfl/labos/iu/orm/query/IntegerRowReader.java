package ch.epfl.labos.iu.orm.query;

import java.sql.ResultSet;
import java.sql.SQLException;

import ch.epfl.labos.iu.orm.Pair;

public class IntegerRowReader implements RowReader<Integer>
{
   QueryStringWithParameters select;
   String alias;
   int column;
   
   Integer postQueryAddIn = null;

   private IntegerRowReader(String query, Object[] parameters, String alias, Integer addIn, int column)
   {
      this.select = new QueryStringWithParameters(query, parameters);
      this.alias = alias;
      postQueryAddIn = addIn;
      this.column = column;
   }
   
   public IntegerRowReader(int addIn)
   {
      postQueryAddIn = addIn;
   }
   
   public IntegerRowReader(String query, Object[] parameters, String alias)
   {
      this.select = new QueryStringWithParameters(query, parameters);
      this.alias = alias;
   }
   
   public Integer readSqlRow(ResultSet rs)
   {
      if (postQueryAddIn != null) return postQueryAddIn;
      try
      {
         return rs.getInt(column);
      } catch (SQLException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return null;
   }

   public void configureQuery(SelectFromWhere query)
   {
      if (postQueryAddIn != null) return;
      column = query.addSelection(select.getQuery(), select.parameters, alias);
   }
   public IntegerRowReader copy()
   {
      IntegerRowReader clone = new IntegerRowReader(select.getQuery(), select.parameters, alias, postQueryAddIn, column);
      return clone;
   }  
   
   public String queryString()
   {
      return select.getQuery();
   }
}
