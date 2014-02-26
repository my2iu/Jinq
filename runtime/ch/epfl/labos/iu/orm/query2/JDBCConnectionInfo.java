package ch.epfl.labos.iu.orm.query2;

import java.io.PrintWriter;
import java.sql.Connection;

public class JDBCConnectionInfo
{
   public Connection connection;
   public int timeout;
   public PrintWriter testOut;
}
