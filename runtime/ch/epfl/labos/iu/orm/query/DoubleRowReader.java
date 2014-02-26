package ch.epfl.labos.iu.orm.query;

import java.sql.ResultSet;
import java.sql.SQLException;

import ch.epfl.labos.iu.orm.Pair;

public class DoubleRowReader implements RowReader<Double>
{
   QueryStringWithParameters select;
   String alias;
   int column;
   
   Double postQueryAddIn = null;

   private DoubleRowReader(String query, Object[] parameters, String alias, Double addIn, int column)
   {
      this.select = new QueryStringWithParameters(query, parameters);
      this.alias = alias;
      postQueryAddIn = addIn;
      this.column = column;
   }
   
   public DoubleRowReader(double addIn)
   {
      postQueryAddIn = addIn;
   }

   public DoubleRowReader(String query, Object[] parameters, String alias)
   {
      this.select = new QueryStringWithParameters(query, parameters);
      this.alias = alias;
   }
   
   
   public Double readSqlRow(ResultSet rs)
   {
      if (postQueryAddIn != null) return postQueryAddIn;
      try
      {
         return rs.getDouble(column);
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
   public DoubleRowReader copy()
   {
      DoubleRowReader clone = new DoubleRowReader(select.getQuery(), select.parameters, alias, postQueryAddIn, column);
      return clone;
   }  

   public String queryString()
   {
      return select.getQuery();
   }
}
