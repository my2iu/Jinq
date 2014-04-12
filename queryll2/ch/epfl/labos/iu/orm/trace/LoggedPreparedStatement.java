package ch.epfl.labos.iu.orm.trace;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

public class LoggedPreparedStatement extends LoggedStatement implements
      PreparedStatement
{
   PreparedStatement ps; 
   public LoggedPreparedStatement(PreparedStatement wrapped)
   {
      super(wrapped);
      ps = wrapped;
   }
   
   public ResultSet executeQuery() throws SQLException
   {
      return ps.executeQuery();
   }

   public int executeUpdate() throws SQLException
   {
      return ps.executeUpdate();
   }

   public void setNull(int parameterIndex, int sqlType) throws SQLException
   {
      ps.setNull(parameterIndex, sqlType);
   }

   public void setBoolean(int parameterIndex, boolean x) throws SQLException
   {
      ps.setBoolean(parameterIndex, x);
   }

   public void setByte(int parameterIndex, byte x) throws SQLException
   {
      ps.setByte(parameterIndex, x);
   }

   public void setShort(int parameterIndex, short x) throws SQLException
   {
      ps.setShort(parameterIndex, x);
   }

   public void setInt(int parameterIndex, int x) throws SQLException
   {
      ps.setInt(parameterIndex, x);
   }

   public void setLong(int parameterIndex, long x) throws SQLException
   {
      ps.setLong(parameterIndex, x);
   }

   public void setFloat(int parameterIndex, float x) throws SQLException
   {
      ps.setFloat(parameterIndex, x);
   }

   public void setDouble(int parameterIndex, double x) throws SQLException
   {
      ps.setDouble(parameterIndex, x);
   }

   public void setBigDecimal(int parameterIndex, BigDecimal x)
         throws SQLException
   {
      ps.setBigDecimal(parameterIndex, x);
   }

   public void setString(int parameterIndex, String x) throws SQLException
   {
      ps.setString(parameterIndex, x);
   }

   public void setBytes(int parameterIndex, byte[] x) throws SQLException
   {
      ps.setBytes(parameterIndex, x);
   }

   public void setDate(int parameterIndex, Date x) throws SQLException
   {
      ps.setDate(parameterIndex, x);
   }

   public void setTime(int parameterIndex, Time x) throws SQLException
   {
      ps.setTime(parameterIndex, x);
   }

   public void setTimestamp(int parameterIndex, Timestamp x)
         throws SQLException
   {
      ps.setTimestamp(parameterIndex, x);
   }

   public void setAsciiStream(int parameterIndex, InputStream x, int length)
         throws SQLException
   {
      ps.setAsciiStream(parameterIndex, x, length);
   }

   public void setUnicodeStream(int parameterIndex, InputStream x, int length)
         throws SQLException
   {
      ps.setUnicodeStream(parameterIndex, x, length);
   }

   public void setBinaryStream(int parameterIndex, InputStream x, int length)
         throws SQLException
   {
      ps.setBinaryStream(parameterIndex, x, length);
   }

   public void clearParameters() throws SQLException
   {
      ps.clearParameters();
   }

   public void setObject(int parameterIndex, Object x, int targetSqlType,
         int scale) throws SQLException
   {
      ps.setObject(parameterIndex, x, targetSqlType,
            scale);
   }

   public void setObject(int parameterIndex, Object x, int targetSqlType)
         throws SQLException
   {
      ps.setObject(parameterIndex, x, targetSqlType);
   }

   public void setObject(int parameterIndex, Object x) throws SQLException
   {
      ps.setObject(parameterIndex, x);
   }

   public boolean execute() throws SQLException
   {
      return ps.execute();
   }

   public void addBatch() throws SQLException
   {
      ps.addBatch();
   }

   public void setCharacterStream(int parameterIndex, Reader reader, int length)
         throws SQLException
   {
      ps.setCharacterStream(parameterIndex, reader, length);
   }

   public void setRef(int i, Ref x) throws SQLException
   {
      ps.setRef(i, x);
   }

   public void setBlob(int i, Blob x) throws SQLException
   {
      ps.setBlob(i, x);
   }

   public void setClob(int i, Clob x) throws SQLException
   {
      ps.setClob(i, x);
   }

   public void setArray(int i, Array x) throws SQLException
   {
      ps.setArray(i, x);
   }

   public ResultSetMetaData getMetaData() throws SQLException
   {
      return ps.getMetaData();
   }

   public void setDate(int parameterIndex, Date x, Calendar cal)
         throws SQLException
   {
      ps.setDate(parameterIndex, x, cal);
   }

   public void setTime(int parameterIndex, Time x, Calendar cal)
         throws SQLException
   {
      ps.setTime(parameterIndex, x, cal);
   }

   public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
         throws SQLException
   {
      ps.setTimestamp(parameterIndex, x, cal);
   }

   public void setNull(int paramIndex, int sqlType, String typeName)
         throws SQLException
   {
      ps.setNull(paramIndex, sqlType, typeName);
   }

   public void setURL(int parameterIndex, URL x) throws SQLException
   {
      ps.setURL(parameterIndex, x);
   }

   public ParameterMetaData getParameterMetaData() throws SQLException
   {
      return ps.getParameterMetaData();
   }

  @Override
  public void closeOnCompletion() throws SQLException
  {
    ps.closeOnCompletion();
  }

  @Override
  public boolean isCloseOnCompletion() throws SQLException
  {
    return ps.isCloseOnCompletion();
  }

  @Override
  public boolean isClosed() throws SQLException
  {
    return ps.isClosed();
  }

  @Override
  public boolean isPoolable() throws SQLException
  {
    return ps.isPoolable();
  }

  @Override
  public void setPoolable(boolean arg0) throws SQLException
  {
    ps.setPoolable(arg0);
  }

  @Override
  public boolean isWrapperFor(Class<?> arg0) throws SQLException
  {
    return ps.isWrapperFor(arg0);
  }

  @Override
  public <T> T unwrap(Class<T> arg0) throws SQLException
  {
    return ps.unwrap(arg0);
  }

  @Override
  public void setAsciiStream(int arg0, InputStream arg1) throws SQLException
  {
    ps.setAsciiStream(arg0, arg1);
  }

  @Override
  public void setAsciiStream(int arg0, InputStream arg1, long arg2)
      throws SQLException
  {
    ps.setAsciiStream(arg0, arg1, arg2);
  }

  @Override
  public void setBinaryStream(int arg0, InputStream arg1) throws SQLException
  {
    ps.setBinaryStream(arg0, arg1);
  }

  @Override
  public void setBinaryStream(int arg0, InputStream arg1, long arg2)
      throws SQLException
  {
    ps.setBinaryStream(arg0, arg1, arg2);
  }

  @Override
  public void setBlob(int arg0, InputStream arg1) throws SQLException
  {
    ps.setBlob(arg0, arg1);
  }

  @Override
  public void setBlob(int arg0, InputStream arg1, long arg2)
      throws SQLException
  {
    ps.setBlob(arg0, arg1, arg2);
  }

  @Override
  public void setCharacterStream(int arg0, Reader arg1) throws SQLException
  {
    ps.setCharacterStream(arg0, arg1);
  }

  @Override
  public void setCharacterStream(int arg0, Reader arg1, long arg2)
      throws SQLException
  {
    ps.setCharacterStream(arg0, arg1, arg2);
  }

  @Override
  public void setClob(int arg0, Reader arg1) throws SQLException
  {
    ps.setClob(arg0, arg1);
  }

  @Override
  public void setClob(int arg0, Reader arg1, long arg2) throws SQLException
  {
    ps.setClob(arg0, arg1, arg2);
  }

  @Override
  public void setNCharacterStream(int arg0, Reader arg1) throws SQLException
  {
    ps.setNCharacterStream(arg0, arg1);
  }

  @Override
  public void setNCharacterStream(int arg0, Reader arg1, long arg2)
      throws SQLException
  {
    ps.setNCharacterStream(arg0, arg1, arg2);
  }

  @Override
  public void setNClob(int arg0, NClob arg1) throws SQLException
  {
    ps.setNClob(arg0, arg1);
  }

  @Override
  public void setNClob(int arg0, Reader arg1) throws SQLException
  {
    ps.setNClob(arg0, arg1);
  }

  @Override
  public void setNClob(int arg0, Reader arg1, long arg2) throws SQLException
  {
    ps.setNClob(arg0, arg1, arg2);
  }

  @Override
  public void setNString(int arg0, String arg1) throws SQLException
  {
    ps.setNString(arg0, arg1);
  }

  @Override
  public void setRowId(int arg0, RowId arg1) throws SQLException
  {
    ps.setRowId(arg0, arg1);
  }

  @Override
  public void setSQLXML(int arg0, SQLXML arg1) throws SQLException
  {
    ps.setSQLXML(arg0, arg1);
  }

}
