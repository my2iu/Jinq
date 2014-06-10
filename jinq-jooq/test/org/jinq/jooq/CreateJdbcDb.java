package org.jinq.jooq;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateJdbcDb
{
   Connection con;
   CreateJdbcDb(Connection con)
   {
      this.con = con;
   }
   
   private void createCustomer(int id, String name, String country, int debt, int salary) throws SQLException
   {
      PreparedStatement stmt = con.prepareStatement("INSERT INTO Customers (CustomerId, Name, Country, Debt, Salary) "
            + "VALUES (?, ?, ?, ?, ?)");
      stmt.setInt(1, id);
      stmt.setString(2, name);
      stmt.setString(3, country);
      stmt.setInt(4, debt);
      stmt.setInt(5, salary);
      stmt.executeUpdate();
      stmt.close();
   }
   
   private void createSale(int saleId, int customerId, String date) throws SQLException
   {
      PreparedStatement stmt = con.prepareStatement("INSERT INTO Sales (SaleId, CustomerId, Date) "
            + "VALUES (?, ?, ?)");
      stmt.setInt(1, saleId);
      stmt.setInt(2, customerId);
      stmt.setString(3, date);
      stmt.executeUpdate();
      stmt.close();
   }
   
   private void createTables(Statement stmt) throws SQLException
   {
      stmt.executeUpdate("create table Customers (" 
            + "CustomerId   INTEGER NOT NULL, " 
            + "Name  VARCHAR(50) NOT NULL, " 
            + "Country VARCHAR(50) NOT NULL, " 
            + "Debt INTEGER NOT NULL, " 
            + "Salary INTEGER NOT NULL, " 
            + "PRIMARY KEY (CustomerId))");
      stmt.executeUpdate("CREATE TABLE Sales ( "
            + "SaleId   INTEGER NOT NULL, "
            + "CustomerId  INTEGER NOT NULL, "
            + "Date VARCHAR(12) NOT NULL, PRIMARY KEY (SaleId))");
      stmt.executeUpdate("CREATE TABLE LineOrders ("
            + "SaleId  INTEGER NOT NULL, "
            + "ItemId  INTEGER NOT NULL, "
            + "Quantity  INTEGER NOT NULL, "
            + "PRIMARY KEY (SaleId, ItemId))");
      stmt.executeUpdate("CREATE TABLE Items ( "
            + "ItemId  INTEGER NOT NULL, "
            + "Name  VARCHAR(50) NOT NULL, "
            + "SalePrice DECIMAL NOT NULL, "
            + "PurchasePrice DECIMAL NOT NULL, "
            + "PRIMARY KEY (ItemId))");
      stmt.executeUpdate("CREATE TABLE ItemSuppliers ("
            + "ItemId  INTEGER NOT NULL, "
            + "SupplierId  INTEGER NOT NULL, "
            + "PRIMARY KEY (ItemId, SupplierId))");
      stmt.executeUpdate("CREATE TABLE Suppliers ("
            + "SupplierId  INTEGER NOT NULL, "
            + "Name  VARCHAR(50) NOT NULL, "
            + "Country  VARCHAR(50) NOT NULL, "
            + "PRIMARY KEY (SupplierId))");
   }
   
   void createDatabase() throws SQLException
   {
      Statement stmt = con.createStatement();
      createTables(stmt);
      stmt.close();
      
      createCustomer(1, "Alice", "Switzerland", 100, 200);
      createCustomer(2, "Bob", "Switzerland", 200, 300);
      createCustomer(3, "Carol", "USA", 300, 250);
      createCustomer(4, "Dave", "UK", 100, 500);
      createCustomer(5, "Eve", "Canada", 10, 30); 

      createSale(1, 1, "2005");
      createSale(2, 1, "2004");
      createSale(3, 3, "2003");
      createSale(4, 3, "2004");
      createSale(5, 4, "2001");
      createSale(6, 5, "2005");

   }
}
