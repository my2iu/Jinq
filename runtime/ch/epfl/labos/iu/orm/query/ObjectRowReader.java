package ch.epfl.labos.iu.orm.query;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ObjectRowReader<T> implements RowReader<T>
{
   QueryStringWithParameters select;
   String alias;
   int column;
   
   T postQueryAddIn = null;

   private ObjectRowReader(String query, Object[] parameters, String alias, T addIn, int column)
   {
      this.select = new QueryStringWithParameters(query, parameters);
      this.alias = alias;
      postQueryAddIn = addIn;
      this.column = column;
   }
   
   public ObjectRowReader(T addIn)
   {
      postQueryAddIn = addIn;
   }
   
   public ObjectRowReader(String query, Object[] parameters, String alias)
   {
      this.select = new QueryStringWithParameters(query, parameters);
      this.alias = alias;
   }
   
   public T readSqlRow(ResultSet rs)
   {
      if (postQueryAddIn != null) return postQueryAddIn;
      try
      {
         return (T)rs.getObject(column);
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
   public ObjectRowReader<T> copy()
   {
      ObjectRowReader<T> clone = new ObjectRowReader<T>(select.getQuery(), select.parameters, alias, postQueryAddIn, column);
      return clone;
   }  
   
   public String queryString()
   {
      return select.getQuery();
   }

}
