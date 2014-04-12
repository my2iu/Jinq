package ch.epfl.labos.iu.orm.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class QueryStringWithParameters
{
   final public String query;
   final public Object[] parameters;
   
   public QueryStringWithParameters(String query, Object[] parameters)
   {
      this.query = query;
      if (parameters == null)
         this.parameters = new Object[0];
      else
         this.parameters = parameters;
   }
   
   int setParameters(PreparedStatement ps, int idx) throws SQLException
   {
      for (Object p: parameters)
      {
         ps.setObject(idx, p);
         idx++;
      }
      return idx;
   }
   
   String getQuery()
   {
      return query;
   }
   
   int numParameters()
   {
      return parameters.length;
   }
   
   public int hashCode() {
      int hash = query.hashCode();
      for (Object p: parameters)
         hash += p.hashCode();
      return hash;
   }
   public boolean equals(Object obj)
   {
      if ( this == obj) return true;
      
      if (! (obj instanceof QueryStringWithParameters)) return false;
      
      QueryStringWithParameters qswp = (QueryStringWithParameters)obj;
      
      if (!qswp.query.equals(query)) return false;
      if (parameters.length != qswp.parameters.length) return false;
      for (int n = 0; n < parameters.length; n++)
         if (!parameters[n].equals(qswp.parameters[n])) return false;
      return true;
   }   
}
