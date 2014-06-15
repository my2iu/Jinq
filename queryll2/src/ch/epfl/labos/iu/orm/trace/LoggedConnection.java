package ch.epfl.labos.iu.orm.trace;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class LoggedConnection implements Connection
{
   Connection wrapped;
   public LoggedConnection(Connection wrapped)
   {
      this.wrapped = wrapped;
   }

   public Statement createStatement() throws SQLException
   {
      return new LoggedStatement(wrapped.createStatement());
   }

   public PreparedStatement prepareStatement(String sql) throws SQLException
   {
      System.out.println("Connection.prepareStatement: " + sql);
      return new LoggedPreparedStatement(wrapped.prepareStatement(sql));
   }

   public CallableStatement prepareCall(String sql) throws SQLException
   {
      return wrapped.prepareCall(sql);
   }

   public String nativeSQL(String sql) throws SQLException
   {
      return wrapped.nativeSQL(sql);
   }

   public void setAutoCommit(boolean autoCommit) throws SQLException
   {
      wrapped.setAutoCommit(autoCommit);
   }

   public boolean getAutoCommit() throws SQLException
   {
      return wrapped.getAutoCommit();
   }

   public void commit() throws SQLException
   {
      wrapped.commit();
   }

   public void rollback() throws SQLException
   {
      wrapped.rollback();
   }

   public void close() throws SQLException
   {
      wrapped.close();
   }

   public boolean isClosed() throws SQLException
   {
      return wrapped.isClosed();
   }

   public DatabaseMetaData getMetaData() throws SQLException
   {
      return wrapped.getMetaData();
   }

   public void setReadOnly(boolean readOnly) throws SQLException
   {
      wrapped.setReadOnly(readOnly);
   }

   public boolean isReadOnly() throws SQLException
   {
      return wrapped.isReadOnly();
   }

   public void setCatalog(String catalog) throws SQLException
   {
      wrapped.setCatalog(catalog);
   }

   public String getCatalog() throws SQLException
   {
      return wrapped.getCatalog();
   }

   public void setTransactionIsolation(int level) throws SQLException
   {
      wrapped.setTransactionIsolation(level);
   }

   public int getTransactionIsolation() throws SQLException
   {
      return wrapped.getTransactionIsolation();
   }

   public SQLWarning getWarnings() throws SQLException
   {
      return wrapped.getWarnings();
   }

   public void clearWarnings() throws SQLException
   {
      wrapped.clearWarnings();
   }

   public Statement createStatement(int resultSetType, int resultSetConcurrency)
         throws SQLException
   {
      return new LoggedStatement(wrapped.createStatement(resultSetType, resultSetConcurrency));
   }

   public PreparedStatement prepareStatement(String sql, int resultSetType,
         int resultSetConcurrency) throws SQLException
   {
      return new LoggedPreparedStatement(wrapped.prepareStatement(sql, resultSetType, resultSetConcurrency));
   }

   public CallableStatement prepareCall(String sql, int resultSetType,
         int resultSetConcurrency) throws SQLException
   {
      return wrapped.prepareCall(sql, resultSetType, resultSetConcurrency);
   }

   public Map<String, Class<?>> getTypeMap() throws SQLException
   {
      return wrapped.getTypeMap();
   }

   public void setTypeMap(Map<String, Class<?>> arg0) throws SQLException
   {
      wrapped.setTypeMap(arg0);
   }

   public void setHoldability(int holdability) throws SQLException
   {
      wrapped.setHoldability(holdability);
   }

   public int getHoldability() throws SQLException
   {
      return wrapped.getHoldability();
   }

   public Savepoint setSavepoint() throws SQLException
   {
      return wrapped.setSavepoint();
   }

   public Savepoint setSavepoint(String name) throws SQLException
   {
      return wrapped.setSavepoint(name);
   }

   public void rollback(Savepoint savepoint) throws SQLException
   {
      wrapped.rollback(savepoint);
   }

   public void releaseSavepoint(Savepoint savepoint) throws SQLException
   {
      wrapped.releaseSavepoint(savepoint);
   }

   public Statement createStatement(int resultSetType,
         int resultSetConcurrency, int resultSetHoldability)
         throws SQLException
   {
      return new LoggedStatement(wrapped.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability));
   }

   public PreparedStatement prepareStatement(String sql, int resultSetType,
         int resultSetConcurrency, int resultSetHoldability)
         throws SQLException
   {
      System.out.println("Connection.prepareStatement: " + sql);
      return new LoggedPreparedStatement(wrapped.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
   }

   public CallableStatement prepareCall(String sql, int resultSetType,
         int resultSetConcurrency, int resultSetHoldability)
         throws SQLException
   {
      return wrapped.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
   }

   public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
         throws SQLException
   {
      System.out.println("Connection.prepareStatement: " + sql);
      return new LoggedPreparedStatement(wrapped.prepareStatement(sql, autoGeneratedKeys));
   }

   public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
         throws SQLException
   {
      System.out.println("Connection.prepareStatement: " + sql);
      return new LoggedPreparedStatement(wrapped.prepareStatement(sql, columnIndexes));
   }

   public PreparedStatement prepareStatement(String sql, String[] columnNames)
         throws SQLException
   {
      System.out.println("Connection.prepareStatement: " + sql);
      return new LoggedPreparedStatement(wrapped.prepareStatement(sql, columnNames));
   }

  @Override
  public boolean isWrapperFor(Class<?> arg0) throws SQLException
  {
    return wrapped.isWrapperFor(arg0);
  }

  @Override
  public <T> T unwrap(Class<T> arg0) throws SQLException
  {
    return wrapped.unwrap(arg0);
  }

  @Override
  public void abort(Executor arg0) throws SQLException
  {
    wrapped.abort(arg0);
  }

  @Override
  public Array createArrayOf(String arg0, Object[] arg1) throws SQLException
  {
    return wrapped.createArrayOf(arg0, arg1);
  }

  @Override
  public Blob createBlob() throws SQLException
  {
    return wrapped.createBlob();
  }

  @Override
  public Clob createClob() throws SQLException
  {
    return wrapped.createClob();
  }

  @Override
  public NClob createNClob() throws SQLException
  {
    return wrapped.createNClob();
  }

  @Override
  public SQLXML createSQLXML() throws SQLException
  {
    return wrapped.createSQLXML();
  }

  @Override
  public Struct createStruct(String arg0, Object[] arg1) throws SQLException
  {
    return wrapped.createStruct(arg0, arg1);
  }

  @Override
  public Properties getClientInfo() throws SQLException
  {
    return wrapped.getClientInfo();
  }

  @Override
  public String getClientInfo(String arg0) throws SQLException
  {
    return wrapped.getClientInfo(arg0);
  }

  @Override
  public int getNetworkTimeout() throws SQLException
  {
    return wrapped.getNetworkTimeout();
  }

  @Override
  public String getSchema() throws SQLException
  {
    return wrapped.getSchema();
  }

  @Override
  public boolean isValid(int arg0) throws SQLException
  {
    return wrapped.isValid(arg0);
  }

  @Override
  public void setClientInfo(Properties arg0) throws SQLClientInfoException
  {
    wrapped.setClientInfo(arg0);
  }

  @Override
  public void setClientInfo(String arg0, String arg1)
      throws SQLClientInfoException
  {
    wrapped.setClientInfo(arg0, arg1);
  }

  @Override
  public void setNetworkTimeout(Executor arg0, int arg1) throws SQLException
  {
    wrapped.setNetworkTimeout(arg0, arg1);
  }

  @Override
  public void setSchema(String arg0) throws SQLException
  {
    wrapped.setSchema(arg0);
  }
}
