package ch.epfl.labos.iu.orm.query2;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JDBCParameterLink
{
   int lambdaIdx;
   int paramOffset;
   public JDBCParameterLink(int lambdaIndex, int paramOffset, ParameterLocation loc)
   {
      this.lambdaIdx = lambdaIndex;
      this.paramOffset = paramOffset;
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
//   public Type type;
//   public Object val;
   public void configureParameters(PreparedStatement stmt, Object[][] params, int n) throws SQLException
   {
      Object val = params[lambdaIdx][paramOffset]; 
       Type type = Type.Object;
       if (val instanceof Integer)
          type = Type.Int;
       else if (val instanceof Float)
          type = Type.Float;
       else if (val instanceof Double)
          type = Type.Double;
       else if (val instanceof String)
          type = Type.String;
       else if (val instanceof java.sql.Date)
          type = Type.Date;
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
