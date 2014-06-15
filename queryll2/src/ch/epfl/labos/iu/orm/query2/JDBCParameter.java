package ch.epfl.labos.iu.orm.query2;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JDBCParameter
{
   public JDBCParameter(Object param)
   {
      type = Type.Object;
      if (param instanceof Integer)
         type = Type.Int;
      else if (param instanceof Float)
         type = Type.Float;
      else if (param instanceof Double)
         type = Type.Double;
      else if (param instanceof String)
         type = Type.String;
      else if (param instanceof java.sql.Date)
         type = Type.Date;
      this.val = param;
   }
   
   // This type stuff is probably unnecessary since we can determine
   // things from the object type
   public static enum Type
   {
      Object,
      Int,
      Float,
      Double,
      String,
      Date
   }
   public Type type;
   public Object val;
   public void configureParameters(PreparedStatement stmt, int n) throws SQLException
   {
      switch(type)
      {
         case Int: stmt.setInt(n, (Integer)val); break;
         case Float: stmt.setFloat(n, (Float)val); break;
         case Double: stmt.setDouble(n, (Double)val); break;
         case String: stmt.setString(n, (String)val); break;
         case Date: stmt.setDate(n, (java.sql.Date)val); break;
         case Object:
         default: stmt.setObject(n, val); break;
      }
   }
}
