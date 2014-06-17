
package org.jinq.test.entities;

import ch.epfl.labos.iu.orm.DBSet;
import ch.epfl.labos.iu.orm.LazySet;
import ch.epfl.labos.iu.orm.VectorSet;
import ch.epfl.labos.iu.orm.Triple;
import ch.epfl.labos.iu.orm.Quartic;
import ch.epfl.labos.iu.orm.Quintic;
import ch.epfl.labos.iu.orm.QueryList;
import ch.epfl.labos.iu.orm.query.RowReader;
import ch.epfl.labos.iu.orm.query.SelectFromWhere;
import ch.epfl.labos.iu.orm.query2.JDBCConnectionInfo;
import ch.epfl.labos.iu.orm.query2.SQLQueryComposer;
import ch.epfl.labos.iu.orm.query2.SQLReader;
import ch.epfl.labos.iu.orm.query2.SQLReaderColumnDescription;
import ch.epfl.labos.iu.orm.query2.EntityManagerBackdoor;

import org.jinq.orm.annotations.EntitySupplier;
import org.jinq.orm.annotations.NoSideEffects;
import org.jinq.orm.stream.JinqStream;
import org.jinq.orm.stream.QueryJinqStream;
import org.jinq.tuples.Pair;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.WeakHashMap;
import java.util.stream.Stream;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.lang.ref.WeakReference;


public class EntityManager implements EntityManagerBackdoor, Serializable
{
   // Note: The EntityManager is not properly serializable, but some queries do access the
   // entity manager. Those Java 8 lambdas need to be serialized to be decoded, so 
   // there needs to be a way to record some sort of placeholder entity manager when
   // serializing the lambda.

   transient DBManager db;
   public int timeout = 0;
   boolean cacheQueries = true;
   boolean partialCacheQueries = true;
   

   public EntityManager(DBManager db)
   {
      this.db = db;
      this.timeout = db.timeout;
      this.cacheQueries = db.useFullQueryCaching;
      this.partialCacheQueries = db.usePartialQueryCaching;

   

   

      {
      
//       SelectFromWhere query = new SelectFromWhere();
//       String prefix = query.addTable("Customers");
//       RowReader <Customer> reader = new CustomerRowReader(prefix);
//       // TODO: Use of db con connection here that needs to be removed
//       customer = new QueryLazySet<Customer>(db.con, query, reader);
         JDBCConnectionInfo jdbc = new JDBCConnectionInfo();
         jdbc.connection = db.con;
         jdbc.timeout = db.timeout;
         jdbc.testOut = db.testOut;
         CustomerSQLReader reader = new CustomerSQLReader(this); 
         SQLQueryComposer<Customer> query =
            new SQLQueryComposer<Customer>(
                  this,
                  jdbc,
                  db.queryll,
                  reader,
                  reader.getColumnNames(),
                  "Customers"
               );
         customerQuery = query;
         customer = new QueryList<Customer>(query);
      }

   
   
      
      
      
   
   
   

      {
      
//       SelectFromWhere query = new SelectFromWhere();
//       String prefix = query.addTable("Sales");
//       RowReader <Sale> reader = new SaleRowReader(prefix);
//       // TODO: Use of db con connection here that needs to be removed
//       sale = new QueryLazySet<Sale>(db.con, query, reader);
         JDBCConnectionInfo jdbc = new JDBCConnectionInfo();
         jdbc.connection = db.con;
         jdbc.timeout = db.timeout;
         jdbc.testOut = db.testOut;
         SaleSQLReader reader = new SaleSQLReader(this); 
         SQLQueryComposer<Sale> query =
            new SQLQueryComposer<Sale>(
                  this,
                  jdbc,
                  db.queryll,
                  reader,
                  reader.getColumnNames(),
                  "Sales"
               );
         saleQuery = query;
         sale = new QueryList<Sale>(query);
      }

   
   
      
      
      
   
   
   

      {
      
//       SelectFromWhere query = new SelectFromWhere();
//       String prefix = query.addTable("LineOrders");
//       RowReader <LineOrder> reader = new LineOrderRowReader(prefix);
//       // TODO: Use of db con connection here that needs to be removed
//       lineOrder = new QueryLazySet<LineOrder>(db.con, query, reader);
         JDBCConnectionInfo jdbc = new JDBCConnectionInfo();
         jdbc.connection = db.con;
         jdbc.timeout = db.timeout;
         jdbc.testOut = db.testOut;
         LineOrderSQLReader reader = new LineOrderSQLReader(this); 
         SQLQueryComposer<LineOrder> query =
            new SQLQueryComposer<LineOrder>(
                  this,
                  jdbc,
                  db.queryll,
                  reader,
                  reader.getColumnNames(),
                  "LineOrders"
               );
         lineOrderQuery = query;
         lineOrder = new QueryList<LineOrder>(query);
      }


   
      
      
      
   

   

      {
      
//       SelectFromWhere query = new SelectFromWhere();
//       String prefix = query.addTable("Items");
//       RowReader <Item> reader = new ItemRowReader(prefix);
//       // TODO: Use of db con connection here that needs to be removed
//       item = new QueryLazySet<Item>(db.con, query, reader);
         JDBCConnectionInfo jdbc = new JDBCConnectionInfo();
         jdbc.connection = db.con;
         jdbc.timeout = db.timeout;
         jdbc.testOut = db.testOut;
         ItemSQLReader reader = new ItemSQLReader(this); 
         SQLQueryComposer<Item> query =
            new SQLQueryComposer<Item>(
                  this,
                  jdbc,
                  db.queryll,
                  reader,
                  reader.getColumnNames(),
                  "Items"
               );
         itemQuery = query;
         item = new QueryList<Item>(query);
      }

   
   

      {
      
//       SelectFromWhere query = new SelectFromWhere();
//       String prefix = query.addTable("ItemSuppliers");
//       RowReader <ItemSupplier> reader = new ItemSupplierRowReader(prefix);
//       // TODO: Use of db con connection here that needs to be removed
//       itemSupplier = new QueryLazySet<ItemSupplier>(db.con, query, reader);
         JDBCConnectionInfo jdbc = new JDBCConnectionInfo();
         jdbc.connection = db.con;
         jdbc.timeout = db.timeout;
         jdbc.testOut = db.testOut;
         ItemSupplierSQLReader reader = new ItemSupplierSQLReader(this); 
         SQLQueryComposer<ItemSupplier> query =
            new SQLQueryComposer<ItemSupplier>(
                  this,
                  jdbc,
                  db.queryll,
                  reader,
                  reader.getColumnNames(),
                  "ItemSuppliers"
               );
         itemSupplierQuery = query;
         itemSupplier = new QueryList<ItemSupplier>(query);
      }

   
   
      
      
      
      
   
   
   

      {
      
//       SelectFromWhere query = new SelectFromWhere();
//       String prefix = query.addTable("Suppliers");
//       RowReader <Supplier> reader = new SupplierRowReader(prefix);
//       // TODO: Use of db con connection here that needs to be removed
//       supplier = new QueryLazySet<Supplier>(db.con, query, reader);
         JDBCConnectionInfo jdbc = new JDBCConnectionInfo();
         jdbc.connection = db.con;
         jdbc.timeout = db.timeout;
         jdbc.testOut = db.testOut;
         SupplierSQLReader reader = new SupplierSQLReader(this); 
         SQLQueryComposer<Supplier> query =
            new SQLQueryComposer<Supplier>(
                  this,
                  jdbc,
                  db.queryll,
                  reader,
                  reader.getColumnNames(),
                  "Suppliers"
               );
         supplierQuery = query;
         supplier = new QueryList<Supplier>(query);
      }


   }

   void flushDirty()
   {
      // TODO: this
      // TODO: Flushing of objects with references to other non-persisted objects that later become persisted

   

   

      for (Customer obj : dirtyCustomer)
      {

      HashMap<String, Object> map = new HashMap<String, Object>();
      map.put("CustomerId", obj.getCustomerId());
      map.put("Name", obj.getName());
      map.put("Country", obj.getCountry());
      map.put("Debt", obj.getDebt());
      map.put("Salary", obj.getSalary());
      map.put("CustomerId", obj.getCustomerId());
          
         String sql = "UPDATE Customers SET ";
         boolean isFirst = true;
         for (Map.Entry entry : map.entrySet())
         {
            if (!isFirst) sql = sql + ", ";
            isFirst = false;
            sql = sql + entry.getKey() + "= ?";
         }
         sql = sql + " WHERE 1=1"
   
         + " AND CustomerId = ? ";
         if (db.testOut != null) db.testOut.println(sql);
         if (sql != null)
         {
            try {
               PreparedStatement stmt = db.con.prepareStatement(sql);
               int idx = 0;
               for (Map.Entry entry : map.entrySet())
               {
                  idx++;
                  stmt.setObject(idx, entry.getValue());
               }
   
               idx++;
               stmt.setObject(idx, obj.getCustomerId());
   
               stmt.executeUpdate();
               stmt.close();
            } catch (SQLException e)
            {
               e.printStackTrace();
            }
         }
   

      }
      dirtyCustomer.clear();

   
   
      
      
      
   
   
   

      for (Sale obj : dirtySale)
      {

      HashMap<String, Object> map = new HashMap<String, Object>();
      map.put("SaleId", obj.getSaleId());
      map.put("Date", obj.getDate());
      map.put("SaleId", obj.getSaleId());
          map.put("CustomerId", obj.getPurchaser().getCustomerId());
        
         String sql = "UPDATE Sales SET ";
         boolean isFirst = true;
         for (Map.Entry entry : map.entrySet())
         {
            if (!isFirst) sql = sql + ", ";
            isFirst = false;
            sql = sql + entry.getKey() + "= ?";
         }
         sql = sql + " WHERE 1=1"
   
         + " AND SaleId = ? ";
         if (db.testOut != null) db.testOut.println(sql);
         if (sql != null)
         {
            try {
               PreparedStatement stmt = db.con.prepareStatement(sql);
               int idx = 0;
               for (Map.Entry entry : map.entrySet())
               {
                  idx++;
                  stmt.setObject(idx, entry.getValue());
               }
   
               idx++;
               stmt.setObject(idx, obj.getSaleId());
   
               stmt.executeUpdate();
               stmt.close();
            } catch (SQLException e)
            {
               e.printStackTrace();
            }
         }
   

      }
      dirtySale.clear();

   
   
      
      
      
   
   
   

      for (LineOrder obj : dirtyLineOrder)
      {

      HashMap<String, Object> map = new HashMap<String, Object>();
      map.put("SaleId", obj.getSaleId());
      map.put("ItemId", obj.getItemId());
      map.put("Quantity", obj.getQuantity());
      map.put("ItemId", obj.getItemId());
          map.put("SaleId", obj.getSaleId());
        
         String sql = "UPDATE LineOrders SET ";
         boolean isFirst = true;
         for (Map.Entry entry : map.entrySet())
         {
            if (!isFirst) sql = sql + ", ";
            isFirst = false;
            sql = sql + entry.getKey() + "= ?";
         }
         sql = sql + " WHERE 1=1"
   
         + " AND SaleId = ? "
         + " AND ItemId = ? ";
         if (db.testOut != null) db.testOut.println(sql);
         if (sql != null)
         {
            try {
               PreparedStatement stmt = db.con.prepareStatement(sql);
               int idx = 0;
               for (Map.Entry entry : map.entrySet())
               {
                  idx++;
                  stmt.setObject(idx, entry.getValue());
               }
   
               idx++;
               stmt.setObject(idx, obj.getSaleId());
   
               idx++;
               stmt.setObject(idx, obj.getItemId());
   
               stmt.executeUpdate();
               stmt.close();
            } catch (SQLException e)
            {
               e.printStackTrace();
            }
         }
   

      }
      dirtyLineOrder.clear();


   
      
      
      
   

   

      for (Item obj : dirtyItem)
      {

      HashMap<String, Object> map = new HashMap<String, Object>();
      map.put("ItemId", obj.getItemId());
      map.put("Name", obj.getName());
      map.put("SalePrice", obj.getSalePrice());
      map.put("PurchasePrice", obj.getPurchasePrice());
      map.put("ItemId", obj.getItemId());
          map.put("ItemId", obj.getItemId());
        
         String sql = "UPDATE Items SET ";
         boolean isFirst = true;
         for (Map.Entry entry : map.entrySet())
         {
            if (!isFirst) sql = sql + ", ";
            isFirst = false;
            sql = sql + entry.getKey() + "= ?";
         }
         sql = sql + " WHERE 1=1"
   
         + " AND ItemId = ? ";
         if (db.testOut != null) db.testOut.println(sql);
         if (sql != null)
         {
            try {
               PreparedStatement stmt = db.con.prepareStatement(sql);
               int idx = 0;
               for (Map.Entry entry : map.entrySet())
               {
                  idx++;
                  stmt.setObject(idx, entry.getValue());
               }
   
               idx++;
               stmt.setObject(idx, obj.getItemId());
   
               stmt.executeUpdate();
               stmt.close();
            } catch (SQLException e)
            {
               e.printStackTrace();
            }
         }
   
         DBSet<Supplier> added = obj.getSuppliers().comparisonClone();
         added.removeAll(originalItem.get(obj).getSuppliers());
         DBSet<Supplier> removed = originalItem.get(obj).getSuppliers().comparisonClone();
         removed.removeAll(obj.getSuppliers());
         for (Supplier toobj: added)
         {
            HashMap<String, Object> linkmap = new HashMap<String, Object>();
   
            linkmap.put("ItemId", obj.getItemId());
   
            linkmap.put("SupplierId", toobj.getSupplierId());
   
            String linksql = "INSERT INTO ItemSuppliers (";
            isFirst = true;
            for (Map.Entry entry : linkmap.entrySet())
            {
               if (!isFirst) linksql = linksql + ", ";
               isFirst = false;
               linksql = linksql + entry.getKey();
            }
            linksql = linksql + ") VALUES (";
            for (int n = 0; n < linkmap.size(); n++)
            {
               if (n != 0) linksql = linksql + ", ";
               linksql = linksql + "?";
            }
              linksql = linksql + ")";
              if (db.testOut != null) db.testOut.println(linksql);
              if (db.con != null)
              {
               try {
                  PreparedStatement stmt = db.con.prepareStatement(linksql);
                  int idx = 0;
                  for (Map.Entry entry : linkmap.entrySet())
                  {
                     idx++;
                     stmt.setObject(idx, entry.getValue());
                  }
                  stmt.executeUpdate();
                  stmt.close();
               } catch (SQLException e)
               {
                  e.printStackTrace();
               }
              }
         }
         for (Supplier toobj: removed)
         {
            HashMap<String, Object> linkmap = new HashMap<String, Object>();
   
            linkmap.put("ItemId", obj.getItemId());
   
            linkmap.put("SupplierId", toobj.getSupplierId());
   
            String linksql = "DELETE FROM ItemSuppliers "
               + " WHERE 1=1";
            for (Map.Entry entry : linkmap.entrySet())
            {
               linksql = linksql + " AND " + entry.getKey() + " = ?";
            }
            if (db.testOut != null) db.testOut.println(linksql);
            if (db.con != null)
            {
               try {
                  PreparedStatement stmt = db.con.prepareStatement(linksql);
                  int idx = 0;
                  for (Map.Entry entry : linkmap.entrySet())
                  {
                     idx++;
                     stmt.setObject(idx, entry.getValue());
                  }
                  stmt.executeUpdate();
                  stmt.close();
               } catch (SQLException e)
               {
                  e.printStackTrace();
               }
            }
         }
   

      }
      dirtyItem.clear();

   
   

      for (ItemSupplier obj : dirtyItemSupplier)
      {

      HashMap<String, Object> map = new HashMap<String, Object>();
      map.put("ItemId", obj.getItemId());
      map.put("SupplierId", obj.getSupplierId());
      
         String sql = "UPDATE ItemSuppliers SET ";
         boolean isFirst = true;
         for (Map.Entry entry : map.entrySet())
         {
            if (!isFirst) sql = sql + ", ";
            isFirst = false;
            sql = sql + entry.getKey() + "= ?";
         }
         sql = sql + " WHERE 1=1"
   
         + " AND ItemId = ? "
         + " AND SupplierId = ? ";
         if (db.testOut != null) db.testOut.println(sql);
         if (sql != null)
         {
            try {
               PreparedStatement stmt = db.con.prepareStatement(sql);
               int idx = 0;
               for (Map.Entry entry : map.entrySet())
               {
                  idx++;
                  stmt.setObject(idx, entry.getValue());
               }
   
               idx++;
               stmt.setObject(idx, obj.getItemId());
   
               idx++;
               stmt.setObject(idx, obj.getSupplierId());
   
               stmt.executeUpdate();
               stmt.close();
            } catch (SQLException e)
            {
               e.printStackTrace();
            }
         }
   

      }
      dirtyItemSupplier.clear();

   
   
      
      
      
      
   
   
   

      for (Supplier obj : dirtySupplier)
      {

      HashMap<String, Object> map = new HashMap<String, Object>();
      map.put("SupplierId", obj.getSupplierId());
      map.put("Name", obj.getName());
      map.put("Country", obj.getCountry());
      map.put("SupplierId", obj.getSupplierId());
        
         String sql = "UPDATE Suppliers SET ";
         boolean isFirst = true;
         for (Map.Entry entry : map.entrySet())
         {
            if (!isFirst) sql = sql + ", ";
            isFirst = false;
            sql = sql + entry.getKey() + "= ?";
         }
         sql = sql + " WHERE 1=1"
   
         + " AND SupplierId = ? ";
         if (db.testOut != null) db.testOut.println(sql);
         if (sql != null)
         {
            try {
               PreparedStatement stmt = db.con.prepareStatement(sql);
               int idx = 0;
               for (Map.Entry entry : map.entrySet())
               {
                  idx++;
                  stmt.setObject(idx, entry.getValue());
               }
   
               idx++;
               stmt.setObject(idx, obj.getSupplierId());
   
               stmt.executeUpdate();
               stmt.close();
            } catch (SQLException e)
            {
               e.printStackTrace();
            }
         }
   

      }
      dirtySupplier.clear();


   }

   public abstract static class EntityReader<T> extends SQLReader <T>
   {
      EntityManager em;
      String entityInternalClassName;
      List<SQLReaderColumnDescription> columns = new Vector<SQLReaderColumnDescription>();
      public EntityReader(EntityManager em, String entityInternalClassName)
      {
         this.em = em;
         this.entityInternalClassName = entityInternalClassName;
      }
      public int getNumColumns()
      {
         // Currently assumes that each entry in the columns list takes
         // up one column
         //
         return columns.size();
      }
      public int getColumnForField(String field)
      {
         for (int n = 0; n < columns.size(); n++)
         {
            if (field.equals(columns.get(n).field))
               return n;
         }
         return -1;
      }
      public int getColumnIndexForColumnName(String col)
      {
         for (int n = 0; n < columns.size(); n++)
         {
            if (col.equals(columns.get(n).columnName))
               return n;
         }
         return -1;
      }
      
      String [] getColumnNames()
      {
         String [] columnNames = new String[columns.size()];
         for (int n = 0; n < columnNames.length; n++)
            columnNames[n] = columns.get(n).columnName;
         return columnNames;
      }
      SQLReader readerForType(String type)
      {
         if (type.equals("int"))
            return new SQLReader.IntegerSQLReader();
         else if (type.equals("float"))
            return new SQLReader.FloatSQLReader();
         else if (type.equals("double"))
            return new SQLReader.DoubleSQLReader();
         else if (type.equals("String"))
            return new SQLReader.StringSQLReader();
         else if (type.equals("Date"))
            return new SQLReader.DateSQLReader();
         return null;
      }
      public SQLReader getReaderForField(String field)
      {
         for (int n = 0; n < columns.size(); n++)
         {
            if (field.equals(columns.get(n).field))
            {
               String type = columns.get(n).type;
               if (type != null)
                  return readerForType(type);
               return null;
            }
         }
         return null;
      }
      public boolean isCastConsistent(String internalName)
      {
         return entityInternalClassName.equals(internalName);
      }
      
   }


   

   

   // TODO: add plural forms of entity names
   transient SQLQueryComposer<Customer> customerQuery;
   transient DBSet<Customer> customer;
   public DBSet<Customer> allCustomer()
   {
      return customer;
   }
   @EntitySupplier(entityClass="com.example.orm.test.entities.Customer") 
   public JinqStream<Customer> customerStream()
   {
      return new QueryJinqStream<>(customerQuery);
   }

   void dispose(Customer obj)
   {
      flushDirty();
      customer.remove(obj);
      // delete from db immediately
      String sql = "DELETE FROM Customers "
         + " WHERE 1=1"
   
         + " AND CustomerId = ? ";
      if (db.testOut != null) db.testOut.println(sql);
      if (db.con != null)
      {
         try {
            PreparedStatement stmt = db.con.prepareStatement(sql);
            int idx = 0;
   
            ++idx;
            stmt.setObject(idx, obj.getCustomerId());
   
            stmt.executeUpdate();
            stmt.close();
         } catch (SQLException e)
         {
            e.printStackTrace();
         }
      }
   }

   void dirtyInstance(Customer obj)
   {
      dirtyCustomer.add(obj);
   }
   
   void newInstance(Customer obj)
   {
      // TODO: Add handling of objects with auto-generated keys
      // TODO: Add handling of objects whose keys are references to other objects

      assert(obj.em == null);
      obj.em = this;
      // Fix references to other objects that happen to be keys

  
      flushDirty();
      // Add to DB immediately
      customer.add(obj);

      HashMap<String, Object> map = new HashMap<String, Object>();
      map.put("CustomerId", obj.getCustomerId());
      map.put("Name", obj.getName());
      map.put("Country", obj.getCountry());
      map.put("Debt", obj.getDebt());
      map.put("Salary", obj.getSalary());
      map.put("CustomerId", obj.getCustomerId());
          
      String sql = "INSERT INTO Customers (";
      boolean isFirst = true;
      for (Map.Entry entry : map.entrySet())
      {
         if (!isFirst) sql = sql + ", ";
         isFirst = false;
         sql = sql + entry.getKey();
      }
      sql = sql + ") VALUES (";
      for (int n = 0; n < map.size(); n++)
      {
         if (n != 0) sql = sql + ", ";
         sql = sql + "?";
      }
        sql = sql + ")";
        if (db.testOut != null) db.testOut.println(sql);
        if (db.con != null)
        {
         try {
            PreparedStatement stmt = db.con.prepareStatement(sql);
            int idx = 1;
            for (Map.Entry entry : map.entrySet())
            {
               stmt.setObject(idx, entry.getValue());
               idx++;
            }
            stmt.executeUpdate();
            stmt.close();
         } catch (SQLException e)
         {
            e.printStackTrace();
         }
        }
      knownCustomer.put(obj.idKey(), new WeakReference<Customer>(obj));
      originalCustomer.put(obj, (Customer)obj.copyForComparison());
   }

   Customer createCustomer(ResultSet rs, int column) throws SQLException
   {
      Customer obj = null;

      Integer key = rs.getInt(column + 0);
      WeakReference<Customer> ref = knownCustomer.get(key);
      if (ref != null) {
         obj = ref.get();
         if (obj != null)
         {
            // I'm not sure if this is necessary, but it's better to be safe
            if (originalCustomer.get(obj) == null)
               originalCustomer.put(obj, (Customer)obj.copyForComparison());
            return obj;
         }
      }

      obj = new Customer(this, rs, column);
      knownCustomer.put(obj.idKey(), new WeakReference<Customer>(obj));
      originalCustomer.put(obj, (Customer)obj.copyForComparison());

      // TODO: this
      return obj;
   }
   
   Customer createCustomer(ResultSet rs, String prefix) throws SQLException
   {
      Customer obj = null;

      Integer key = rs.getInt(prefix + "_CustomerId");
      WeakReference<Customer> ref = knownCustomer.get(key);
      if (ref != null) {
         obj = ref.get();
         if (obj != null)
         {
            // I'm not sure if this is necessary, but it's better to be safe
            if (originalCustomer.get(obj) == null)
               originalCustomer.put(obj, (Customer)obj.copyForComparison());
            return obj;
         }
      }

      obj = new Customer(this, rs, prefix);
      knownCustomer.put(obj.idKey(), new WeakReference<Customer>(obj));
      originalCustomer.put(obj, (Customer)obj.copyForComparison());

      // TODO: this
      return obj;
   }
   
   transient HashSet<Customer> dirtyCustomer = new HashSet<Customer>(); 
   transient WeakHashMap<Customer, Customer> originalCustomer = new WeakHashMap<Customer, Customer>();   
   transient WeakHashMap<Integer, WeakReference<Customer>> knownCustomer = new WeakHashMap<Integer, WeakReference<Customer>>();  

   public static class CustomerSQLReader extends EntityReader<Customer>
   {
      public CustomerSQLReader(EntityManager em)
      {
         super(em, "org/jinq/test/entities/Customer");
      
         columns.add(new SQLReaderColumnDescription("CustomerId", "CustomerId", "int"));
      
         columns.add(new SQLReaderColumnDescription("Name", "Name", "String"));
      
         columns.add(new SQLReaderColumnDescription("Country", "Country", "String"));
      
         columns.add(new SQLReaderColumnDescription("Debt", "Debt", "int"));
      
         columns.add(new SQLReaderColumnDescription("Salary", "Salary", "int"));
      
      }
      public Customer readData(ResultSet result, int column)
      {
         try {
            if (em.db.isQueryOnly)
               return new Customer(em, result, column);
            else
               return em.createCustomer(result, column);
         } catch (SQLException e)
         {
            e.printStackTrace();
            return null;
         }
      }
   }

   public class CustomerRowReader implements RowReader <Customer>
   {
      public int column;
      public String prefix;
      public CustomerRowReader(String prefix)
      {
         this.prefix = prefix;
      }
      protected CustomerRowReader(String prefix, int column)
      {
         this.prefix = prefix;
         this.column = column;
      }
      public Customer readSqlRow(ResultSet rs)
      {
         try {
            return createCustomer(rs, column);
         } catch (SQLException e)
         {
            e.printStackTrace();
         }
         return null;
      }
      public void configureQuery(SelectFromWhere query)
      {
         int firstColumn =  -1;
         int col;

      
         col = query.addSelection(prefix + ".CustomerId", prefix + "_CustomerId");
         if (firstColumn == -1) firstColumn = col;
      
         col = query.addSelection(prefix + ".Name", prefix + "_Name");
         if (firstColumn == -1) firstColumn = col;
      
         col = query.addSelection(prefix + ".Country", prefix + "_Country");
         if (firstColumn == -1) firstColumn = col;
      
         col = query.addSelection(prefix + ".Debt", prefix + "_Debt");
         if (firstColumn == -1) firstColumn = col;
      
         col = query.addSelection(prefix + ".Salary", prefix + "_Salary");
         if (firstColumn == -1) firstColumn = col;
      
         column = firstColumn;
      }
      public RowReader <Customer> copy()
      {
         return new CustomerRowReader(prefix, column);
      }
      public String queryString()
      {
         return prefix;
      }
   

      String PurchasesPrefix;
      public String materializePurchasesJoinString(SelectFromWhere query)
      {
         if (PurchasesPrefix == null)
         {
         PurchasesPrefix = query.addTable("Sales");
         
            query.addWhereClause(prefix + ".CustomerId = " + PurchasesPrefix + ".CustomerId");
         
         }
         return PurchasesPrefix;
      }
      public SaleRowReader materializePurchasesJoinRowReader(SelectFromWhere query)
      {
         return new SaleRowReader(materializePurchasesJoinString(query));
      }
   
   }

   
   
      
      
      
   
   
   

   // TODO: add plural forms of entity names
   transient SQLQueryComposer<Sale> saleQuery;
   transient DBSet<Sale> sale;
   public DBSet<Sale> allSale()
   {
      return sale;
   }
   @EntitySupplier(entityClass="com.example.orm.test.entities.Sale") 
   public JinqStream<Sale> saleStream()
   {
      return new QueryJinqStream<>(saleQuery);
   }

   void dispose(Sale obj)
   {
      flushDirty();
      sale.remove(obj);
      // delete from db immediately
      String sql = "DELETE FROM Sales "
         + " WHERE 1=1"
   
         + " AND SaleId = ? ";
      if (db.testOut != null) db.testOut.println(sql);
      if (db.con != null)
      {
         try {
            PreparedStatement stmt = db.con.prepareStatement(sql);
            int idx = 0;
   
            ++idx;
            stmt.setObject(idx, obj.getSaleId());
   
            stmt.executeUpdate();
            stmt.close();
         } catch (SQLException e)
         {
            e.printStackTrace();
         }
      }
   }

   void dirtyInstance(Sale obj)
   {
      dirtySale.add(obj);
   }
   
   void newInstance(Sale obj)
   {
      // TODO: Add handling of objects with auto-generated keys
      // TODO: Add handling of objects whose keys are references to other objects

      assert(obj.em == null);
      obj.em = this;
      // Fix references to other objects that happen to be keys

  
      flushDirty();
      // Add to DB immediately
      sale.add(obj);

      HashMap<String, Object> map = new HashMap<String, Object>();
      map.put("SaleId", obj.getSaleId());
      map.put("Date", obj.getDate());
      map.put("SaleId", obj.getSaleId());
          map.put("CustomerId", obj.getPurchaser().getCustomerId());
        
      String sql = "INSERT INTO Sales (";
      boolean isFirst = true;
      for (Map.Entry entry : map.entrySet())
      {
         if (!isFirst) sql = sql + ", ";
         isFirst = false;
         sql = sql + entry.getKey();
      }
      sql = sql + ") VALUES (";
      for (int n = 0; n < map.size(); n++)
      {
         if (n != 0) sql = sql + ", ";
         sql = sql + "?";
      }
        sql = sql + ")";
        if (db.testOut != null) db.testOut.println(sql);
        if (db.con != null)
        {
         try {
            PreparedStatement stmt = db.con.prepareStatement(sql);
            int idx = 1;
            for (Map.Entry entry : map.entrySet())
            {
               stmt.setObject(idx, entry.getValue());
               idx++;
            }
            stmt.executeUpdate();
            stmt.close();
         } catch (SQLException e)
         {
            e.printStackTrace();
         }
        }
      knownSale.put(obj.idKey(), new WeakReference<Sale>(obj));
      originalSale.put(obj, (Sale)obj.copyForComparison());
   }

   Sale createSale(ResultSet rs, int column) throws SQLException
   {
      Sale obj = null;

      Integer key = rs.getInt(column + 0);
      WeakReference<Sale> ref = knownSale.get(key);
      if (ref != null) {
         obj = ref.get();
         if (obj != null)
         {
            // I'm not sure if this is necessary, but it's better to be safe
            if (originalSale.get(obj) == null)
               originalSale.put(obj, (Sale)obj.copyForComparison());
            return obj;
         }
      }

      obj = new Sale(this, rs, column);
      knownSale.put(obj.idKey(), new WeakReference<Sale>(obj));
      originalSale.put(obj, (Sale)obj.copyForComparison());

      // TODO: this
      return obj;
   }
   
   Sale createSale(ResultSet rs, String prefix) throws SQLException
   {
      Sale obj = null;

      Integer key = rs.getInt(prefix + "_SaleId");
      WeakReference<Sale> ref = knownSale.get(key);
      if (ref != null) {
         obj = ref.get();
         if (obj != null)
         {
            // I'm not sure if this is necessary, but it's better to be safe
            if (originalSale.get(obj) == null)
               originalSale.put(obj, (Sale)obj.copyForComparison());
            return obj;
         }
      }

      obj = new Sale(this, rs, prefix);
      knownSale.put(obj.idKey(), new WeakReference<Sale>(obj));
      originalSale.put(obj, (Sale)obj.copyForComparison());

      // TODO: this
      return obj;
   }
   
   transient HashSet<Sale> dirtySale = new HashSet<Sale>(); 
   transient WeakHashMap<Sale, Sale> originalSale = new WeakHashMap<Sale, Sale>();  
   transient WeakHashMap<Integer, WeakReference<Sale>> knownSale = new WeakHashMap<Integer, WeakReference<Sale>>();  

   public static class SaleSQLReader extends EntityReader<Sale>
   {
      public SaleSQLReader(EntityManager em)
      {
         super(em, "org/jinq/test/entities/Sale");
      
         columns.add(new SQLReaderColumnDescription("SaleId", "SaleId", "int"));
      
         columns.add(new SQLReaderColumnDescription("Date", "Date", "String"));
      
         columns.add(new SQLReaderColumnDescription(null, "CustomerId", null));
            
      }
      public Sale readData(ResultSet result, int column)
      {
         try {
            if (em.db.isQueryOnly)
               return new Sale(em, result, column);
            else
               return em.createSale(result, column);
         } catch (SQLException e)
         {
            e.printStackTrace();
            return null;
         }
      }
   }

   public class SaleRowReader implements RowReader <Sale>
   {
      public int column;
      public String prefix;
      public SaleRowReader(String prefix)
      {
         this.prefix = prefix;
      }
      protected SaleRowReader(String prefix, int column)
      {
         this.prefix = prefix;
         this.column = column;
      }
      public Sale readSqlRow(ResultSet rs)
      {
         try {
            return createSale(rs, column);
         } catch (SQLException e)
         {
            e.printStackTrace();
         }
         return null;
      }
      public void configureQuery(SelectFromWhere query)
      {
         int firstColumn =  -1;
         int col;

      
         col = query.addSelection(prefix + ".SaleId", prefix + "_SaleId");
         if (firstColumn == -1) firstColumn = col;
      
         col = query.addSelection(prefix + ".Date", prefix + "_Date");
         if (firstColumn == -1) firstColumn = col;
      
         col = query.addSelection(prefix + ".CustomerId", "LINK_" + prefix + "_CustomerId");
         if (firstColumn == -1) firstColumn = col;
      
         column = firstColumn;
      }
      public RowReader <Sale> copy()
      {
         return new SaleRowReader(prefix, column);
      }
      public String queryString()
      {
         return prefix;
      }
   

      String PurchaserPrefix;
      public String materializePurchaserJoinString(SelectFromWhere query)
      {
         if (PurchaserPrefix == null)
         {
         PurchaserPrefix = query.addTable("Customers");
         
            query.addWhereClause(PurchaserPrefix + ".CustomerId = " + prefix + ".CustomerId");
         
         }
         return PurchaserPrefix;
      }
      public CustomerRowReader materializePurchaserJoinRowReader(SelectFromWhere query)
      {
         return new CustomerRowReader(materializePurchaserJoinString(query));
      }
   

      String SaleLinePrefix;
      public String materializeSaleLineJoinString(SelectFromWhere query)
      {
         if (SaleLinePrefix == null)
         {
         SaleLinePrefix = query.addTable("LineOrders");
         
            query.addWhereClause(prefix + ".SaleId = " + SaleLinePrefix + ".SaleId");
         
         }
         return SaleLinePrefix;
      }
      public LineOrderRowReader materializeSaleLineJoinRowReader(SelectFromWhere query)
      {
         return new LineOrderRowReader(materializeSaleLineJoinString(query));
      }
   
   }

   
   
      
      
      
   
   
   

   // TODO: add plural forms of entity names
   transient SQLQueryComposer<LineOrder> lineOrderQuery;
   transient DBSet<LineOrder> lineOrder;
   public DBSet<LineOrder> allLineOrder()
   {
      return lineOrder;
   }
   @EntitySupplier(entityClass="com.example.orm.test.entities.LineOrder") 
   public JinqStream<LineOrder> lineOrderStream()
   {
      return new QueryJinqStream<>(lineOrderQuery);
   }

   void dispose(LineOrder obj)
   {
      flushDirty();
      lineOrder.remove(obj);
      // delete from db immediately
      String sql = "DELETE FROM LineOrders "
         + " WHERE 1=1"
   
         + " AND SaleId = ? "
         + " AND ItemId = ? ";
      if (db.testOut != null) db.testOut.println(sql);
      if (db.con != null)
      {
         try {
            PreparedStatement stmt = db.con.prepareStatement(sql);
            int idx = 0;
   
            ++idx;
            stmt.setObject(idx, obj.getSaleId());
   
            ++idx;
            stmt.setObject(idx, obj.getItemId());
   
            stmt.executeUpdate();
            stmt.close();
         } catch (SQLException e)
         {
            e.printStackTrace();
         }
      }
   }

   void dirtyInstance(LineOrder obj)
   {
      dirtyLineOrder.add(obj);
   }
   
   void newInstance(LineOrder obj)
   {
      // TODO: Add handling of objects with auto-generated keys
      // TODO: Add handling of objects whose keys are references to other objects

      assert(obj.em == null);
      obj.em = this;
      // Fix references to other objects that happen to be keys

  
      obj.setItemId( 
          obj.getItem().getItemId());
         
      obj.setSaleId(
          obj.getSale().getSaleId());
        
      flushDirty();
      // Add to DB immediately
      lineOrder.add(obj);

      HashMap<String, Object> map = new HashMap<String, Object>();
      map.put("SaleId", obj.getSaleId());
      map.put("ItemId", obj.getItemId());
      map.put("Quantity", obj.getQuantity());
      map.put("ItemId", obj.getItemId());
          map.put("SaleId", obj.getSaleId());
        
      String sql = "INSERT INTO LineOrders (";
      boolean isFirst = true;
      for (Map.Entry entry : map.entrySet())
      {
         if (!isFirst) sql = sql + ", ";
         isFirst = false;
         sql = sql + entry.getKey();
      }
      sql = sql + ") VALUES (";
      for (int n = 0; n < map.size(); n++)
      {
         if (n != 0) sql = sql + ", ";
         sql = sql + "?";
      }
        sql = sql + ")";
        if (db.testOut != null) db.testOut.println(sql);
        if (db.con != null)
        {
         try {
            PreparedStatement stmt = db.con.prepareStatement(sql);
            int idx = 1;
            for (Map.Entry entry : map.entrySet())
            {
               stmt.setObject(idx, entry.getValue());
               idx++;
            }
            stmt.executeUpdate();
            stmt.close();
         } catch (SQLException e)
         {
            e.printStackTrace();
         }
        }
      knownLineOrder.put(obj.idKey(), new WeakReference<LineOrder>(obj));
      originalLineOrder.put(obj, (LineOrder)obj.copyForComparison());
   }

   LineOrder createLineOrder(ResultSet rs, int column) throws SQLException
   {
      LineOrder obj = null;

      Pair<Integer, Integer> key = new Pair<Integer, Integer>(rs.getInt(column + 0), rs.getInt(column + 1));
      WeakReference<LineOrder> ref = knownLineOrder.get(key);
      if (ref != null) {
         obj = ref.get();
         if (obj != null)
         {
            // I'm not sure if this is necessary, but it's better to be safe
            if (originalLineOrder.get(obj) == null)
               originalLineOrder.put(obj, (LineOrder)obj.copyForComparison());
            return obj;
         }
      }

      obj = new LineOrder(this, rs, column);
      knownLineOrder.put(obj.idKey(), new WeakReference<LineOrder>(obj));
      originalLineOrder.put(obj, (LineOrder)obj.copyForComparison());

      // TODO: this
      return obj;
   }
   
   LineOrder createLineOrder(ResultSet rs, String prefix) throws SQLException
   {
      LineOrder obj = null;

      Pair<Integer, Integer> key = new Pair<Integer, Integer>(rs.getInt(prefix + "_SaleId"), rs.getInt(prefix + "_ItemId"));
      WeakReference<LineOrder> ref = knownLineOrder.get(key);
      if (ref != null) {
         obj = ref.get();
         if (obj != null)
         {
            // I'm not sure if this is necessary, but it's better to be safe
            if (originalLineOrder.get(obj) == null)
               originalLineOrder.put(obj, (LineOrder)obj.copyForComparison());
            return obj;
         }
      }

      obj = new LineOrder(this, rs, prefix);
      knownLineOrder.put(obj.idKey(), new WeakReference<LineOrder>(obj));
      originalLineOrder.put(obj, (LineOrder)obj.copyForComparison());

      // TODO: this
      return obj;
   }
   
   transient HashSet<LineOrder> dirtyLineOrder = new HashSet<LineOrder>(); 
   transient WeakHashMap<LineOrder, LineOrder> originalLineOrder = new WeakHashMap<LineOrder, LineOrder>(); 
   transient WeakHashMap<Pair<Integer, Integer>, WeakReference<LineOrder>> knownLineOrder = new WeakHashMap<Pair<Integer, Integer>, WeakReference<LineOrder>>();  

   public static class LineOrderSQLReader extends EntityReader<LineOrder>
   {
      public LineOrderSQLReader(EntityManager em)
      {
         super(em, "org/jinq/test/entities/LineOrder");
      
         columns.add(new SQLReaderColumnDescription("SaleId", "SaleId", "int"));
      
         columns.add(new SQLReaderColumnDescription("ItemId", "ItemId", "int"));
      
         columns.add(new SQLReaderColumnDescription("Quantity", "Quantity", "int"));
      
         columns.add(new SQLReaderColumnDescription(null, "ItemId", null));
            
         columns.add(new SQLReaderColumnDescription(null, "SaleId", null));
            
      }
      public LineOrder readData(ResultSet result, int column)
      {
         try {
            if (em.db.isQueryOnly)
               return new LineOrder(em, result, column);
            else
               return em.createLineOrder(result, column);
         } catch (SQLException e)
         {
            e.printStackTrace();
            return null;
         }
      }
   }

   public class LineOrderRowReader implements RowReader <LineOrder>
   {
      public int column;
      public String prefix;
      public LineOrderRowReader(String prefix)
      {
         this.prefix = prefix;
      }
      protected LineOrderRowReader(String prefix, int column)
      {
         this.prefix = prefix;
         this.column = column;
      }
      public LineOrder readSqlRow(ResultSet rs)
      {
         try {
            return createLineOrder(rs, column);
         } catch (SQLException e)
         {
            e.printStackTrace();
         }
         return null;
      }
      public void configureQuery(SelectFromWhere query)
      {
         int firstColumn =  -1;
         int col;

      
         col = query.addSelection(prefix + ".SaleId", prefix + "_SaleId");
         if (firstColumn == -1) firstColumn = col;
      
         col = query.addSelection(prefix + ".ItemId", prefix + "_ItemId");
         if (firstColumn == -1) firstColumn = col;
      
         col = query.addSelection(prefix + ".Quantity", prefix + "_Quantity");
         if (firstColumn == -1) firstColumn = col;
      
         col = query.addSelection(prefix + ".SaleId", "LINK_" + prefix + "_SaleId");
         if (firstColumn == -1) firstColumn = col;
      
         col = query.addSelection(prefix + ".ItemId", "LINK_" + prefix + "_ItemId");
         if (firstColumn == -1) firstColumn = col;
      
         column = firstColumn;
      }
      public RowReader <LineOrder> copy()
      {
         return new LineOrderRowReader(prefix, column);
      }
      public String queryString()
      {
         return prefix;
      }
   

      String SalePrefix;
      public String materializeSaleJoinString(SelectFromWhere query)
      {
         if (SalePrefix == null)
         {
         SalePrefix = query.addTable("Sales");
         
            query.addWhereClause(SalePrefix + ".SaleId = " + prefix + ".SaleId");
         
         }
         return SalePrefix;
      }
      public SaleRowReader materializeSaleJoinRowReader(SelectFromWhere query)
      {
         return new SaleRowReader(materializeSaleJoinString(query));
      }
   

      String ItemPrefix;
      public String materializeItemJoinString(SelectFromWhere query)
      {
         if (ItemPrefix == null)
         {
         ItemPrefix = query.addTable("Items");
         
            query.addWhereClause(prefix + ".ItemId = " + ItemPrefix + ".ItemId");
         
         }
         return ItemPrefix;
      }
      public ItemRowReader materializeItemJoinRowReader(SelectFromWhere query)
      {
         return new ItemRowReader(materializeItemJoinString(query));
      }
   
   }


   
      
      
      
   

   

   // TODO: add plural forms of entity names
   transient SQLQueryComposer<Item> itemQuery;
   transient DBSet<Item> item;
   public DBSet<Item> allItem()
   {
      return item;
   }
   @EntitySupplier(entityClass="com.example.orm.test.entities.Item") 
   public JinqStream<Item> itemStream()
   {
      return new QueryJinqStream<>(itemQuery);
   }

   void dispose(Item obj)
   {
      flushDirty();
      item.remove(obj);
      // delete from db immediately
      String sql = "DELETE FROM Items "
         + " WHERE 1=1"
   
         + " AND ItemId = ? ";
      if (db.testOut != null) db.testOut.println(sql);
      if (db.con != null)
      {
         try {
            PreparedStatement stmt = db.con.prepareStatement(sql);
            int idx = 0;
   
            ++idx;
            stmt.setObject(idx, obj.getItemId());
   
            stmt.executeUpdate();
            stmt.close();
         } catch (SQLException e)
         {
            e.printStackTrace();
         }
      }
   }

   void dirtyInstance(Item obj)
   {
      dirtyItem.add(obj);
   }
   
   void newInstance(Item obj)
   {
      // TODO: Add handling of objects with auto-generated keys
      // TODO: Add handling of objects whose keys are references to other objects

      assert(obj.em == null);
      obj.em = this;
      // Fix references to other objects that happen to be keys

  
      flushDirty();
      // Add to DB immediately
      item.add(obj);

      HashMap<String, Object> map = new HashMap<String, Object>();
      map.put("ItemId", obj.getItemId());
      map.put("Name", obj.getName());
      map.put("SalePrice", obj.getSalePrice());
      map.put("PurchasePrice", obj.getPurchasePrice());
      map.put("ItemId", obj.getItemId());
          map.put("ItemId", obj.getItemId());
        
      String sql = "INSERT INTO Items (";
      boolean isFirst = true;
      for (Map.Entry entry : map.entrySet())
      {
         if (!isFirst) sql = sql + ", ";
         isFirst = false;
         sql = sql + entry.getKey();
      }
      sql = sql + ") VALUES (";
      for (int n = 0; n < map.size(); n++)
      {
         if (n != 0) sql = sql + ", ";
         sql = sql + "?";
      }
        sql = sql + ")";
        if (db.testOut != null) db.testOut.println(sql);
        if (db.con != null)
        {
         try {
            PreparedStatement stmt = db.con.prepareStatement(sql);
            int idx = 1;
            for (Map.Entry entry : map.entrySet())
            {
               stmt.setObject(idx, entry.getValue());
               idx++;
            }
            stmt.executeUpdate();
            stmt.close();
         } catch (SQLException e)
         {
            e.printStackTrace();
         }
        }
      knownItem.put(obj.idKey(), new WeakReference<Item>(obj));
      originalItem.put(obj, (Item)obj.copyForComparison());
   }

   Item createItem(ResultSet rs, int column) throws SQLException
   {
      Item obj = null;

      Integer key = rs.getInt(column + 0);
      WeakReference<Item> ref = knownItem.get(key);
      if (ref != null) {
         obj = ref.get();
         if (obj != null)
         {
            // I'm not sure if this is necessary, but it's better to be safe
            if (originalItem.get(obj) == null)
               originalItem.put(obj, (Item)obj.copyForComparison());
            return obj;
         }
      }

      obj = new Item(this, rs, column);
      knownItem.put(obj.idKey(), new WeakReference<Item>(obj));
      originalItem.put(obj, (Item)obj.copyForComparison());

      // TODO: this
      return obj;
   }
   
   Item createItem(ResultSet rs, String prefix) throws SQLException
   {
      Item obj = null;

      Integer key = rs.getInt(prefix + "_ItemId");
      WeakReference<Item> ref = knownItem.get(key);
      if (ref != null) {
         obj = ref.get();
         if (obj != null)
         {
            // I'm not sure if this is necessary, but it's better to be safe
            if (originalItem.get(obj) == null)
               originalItem.put(obj, (Item)obj.copyForComparison());
            return obj;
         }
      }

      obj = new Item(this, rs, prefix);
      knownItem.put(obj.idKey(), new WeakReference<Item>(obj));
      originalItem.put(obj, (Item)obj.copyForComparison());

      // TODO: this
      return obj;
   }
   
   transient HashSet<Item> dirtyItem = new HashSet<Item>(); 
   transient WeakHashMap<Item, Item> originalItem = new WeakHashMap<Item, Item>();  
   transient WeakHashMap<Integer, WeakReference<Item>> knownItem = new WeakHashMap<Integer, WeakReference<Item>>();  

   public static class ItemSQLReader extends EntityReader<Item>
   {
      public ItemSQLReader(EntityManager em)
      {
         super(em, "org/jinq/test/entities/Item");
      
         columns.add(new SQLReaderColumnDescription("ItemId", "ItemId", "int"));
      
         columns.add(new SQLReaderColumnDescription("Name", "Name", "String"));
      
         columns.add(new SQLReaderColumnDescription("SalePrice", "SalePrice", "double"));
      
         columns.add(new SQLReaderColumnDescription("PurchasePrice", "PurchasePrice", "double"));
      
      }
      public Item readData(ResultSet result, int column)
      {
         try {
            if (em.db.isQueryOnly)
               return new Item(em, result, column);
            else
               return em.createItem(result, column);
         } catch (SQLException e)
         {
            e.printStackTrace();
            return null;
         }
      }
   }

   public class ItemRowReader implements RowReader <Item>
   {
      public int column;
      public String prefix;
      public ItemRowReader(String prefix)
      {
         this.prefix = prefix;
      }
      protected ItemRowReader(String prefix, int column)
      {
         this.prefix = prefix;
         this.column = column;
      }
      public Item readSqlRow(ResultSet rs)
      {
         try {
            return createItem(rs, column);
         } catch (SQLException e)
         {
            e.printStackTrace();
         }
         return null;
      }
      public void configureQuery(SelectFromWhere query)
      {
         int firstColumn =  -1;
         int col;

      
         col = query.addSelection(prefix + ".ItemId", prefix + "_ItemId");
         if (firstColumn == -1) firstColumn = col;
      
         col = query.addSelection(prefix + ".Name", prefix + "_Name");
         if (firstColumn == -1) firstColumn = col;
      
         col = query.addSelection(prefix + ".SalePrice", prefix + "_SalePrice");
         if (firstColumn == -1) firstColumn = col;
      
         col = query.addSelection(prefix + ".PurchasePrice", prefix + "_PurchasePrice");
         if (firstColumn == -1) firstColumn = col;
      
         column = firstColumn;
      }
      public RowReader <Item> copy()
      {
         return new ItemRowReader(prefix, column);
      }
      public String queryString()
      {
         return prefix;
      }
   

      String OrdersPrefix;
      public String materializeOrdersJoinString(SelectFromWhere query)
      {
         if (OrdersPrefix == null)
         {
         OrdersPrefix = query.addTable("LineOrders");
         
            query.addWhereClause(OrdersPrefix + ".ItemId = " + prefix + ".ItemId");
         
         }
         return OrdersPrefix;
      }
      public LineOrderRowReader materializeOrdersJoinRowReader(SelectFromWhere query)
      {
         return new LineOrderRowReader(materializeOrdersJoinString(query));
      }
   

      String SuppliersPrefix;
      public String materializeSuppliersJoinString(SelectFromWhere query)
      {
         if (SuppliersPrefix == null)
         {
         
            String middlePrefix = query.addTable("ItemSuppliers");
         SuppliersPrefix = query.addTable("Suppliers");
         
            query.addWhereClause(prefix + ".ItemId = " + middlePrefix + ".ItemId");
         
            query.addWhereClause(middlePrefix + ".SupplierId = " + SuppliersPrefix + ".SupplierId");
         
         }
         return SuppliersPrefix;
      }
      public SupplierRowReader materializeSuppliersJoinRowReader(SelectFromWhere query)
      {
         return new SupplierRowReader(materializeSuppliersJoinString(query));
      }
   
   }

   
   

   // TODO: add plural forms of entity names
   transient SQLQueryComposer<ItemSupplier> itemSupplierQuery;
   transient DBSet<ItemSupplier> itemSupplier;
   public DBSet<ItemSupplier> allItemSupplier()
   {
      return itemSupplier;
   }
   @EntitySupplier(entityClass="com.example.orm.test.entities.ItemSupplier") 
   public JinqStream<ItemSupplier> itemSupplierStream()
   {
      return new QueryJinqStream<>(itemSupplierQuery);
   }

   void dispose(ItemSupplier obj)
   {
      flushDirty();
      itemSupplier.remove(obj);
      // delete from db immediately
      String sql = "DELETE FROM ItemSuppliers "
         + " WHERE 1=1"
   
         + " AND ItemId = ? "
         + " AND SupplierId = ? ";
      if (db.testOut != null) db.testOut.println(sql);
      if (db.con != null)
      {
         try {
            PreparedStatement stmt = db.con.prepareStatement(sql);
            int idx = 0;
   
            ++idx;
            stmt.setObject(idx, obj.getItemId());
   
            ++idx;
            stmt.setObject(idx, obj.getSupplierId());
   
            stmt.executeUpdate();
            stmt.close();
         } catch (SQLException e)
         {
            e.printStackTrace();
         }
      }
   }

   void dirtyInstance(ItemSupplier obj)
   {
      dirtyItemSupplier.add(obj);
   }
   
   void newInstance(ItemSupplier obj)
   {
      // TODO: Add handling of objects with auto-generated keys
      // TODO: Add handling of objects whose keys are references to other objects

      assert(obj.em == null);
      obj.em = this;
      // Fix references to other objects that happen to be keys

  
      flushDirty();
      // Add to DB immediately
      itemSupplier.add(obj);

      HashMap<String, Object> map = new HashMap<String, Object>();
      map.put("ItemId", obj.getItemId());
      map.put("SupplierId", obj.getSupplierId());
      
      String sql = "INSERT INTO ItemSuppliers (";
      boolean isFirst = true;
      for (Map.Entry entry : map.entrySet())
      {
         if (!isFirst) sql = sql + ", ";
         isFirst = false;
         sql = sql + entry.getKey();
      }
      sql = sql + ") VALUES (";
      for (int n = 0; n < map.size(); n++)
      {
         if (n != 0) sql = sql + ", ";
         sql = sql + "?";
      }
        sql = sql + ")";
        if (db.testOut != null) db.testOut.println(sql);
        if (db.con != null)
        {
         try {
            PreparedStatement stmt = db.con.prepareStatement(sql);
            int idx = 1;
            for (Map.Entry entry : map.entrySet())
            {
               stmt.setObject(idx, entry.getValue());
               idx++;
            }
            stmt.executeUpdate();
            stmt.close();
         } catch (SQLException e)
         {
            e.printStackTrace();
         }
        }
      knownItemSupplier.put(obj.idKey(), new WeakReference<ItemSupplier>(obj));
      originalItemSupplier.put(obj, (ItemSupplier)obj.copyForComparison());
   }

   ItemSupplier createItemSupplier(ResultSet rs, int column) throws SQLException
   {
      ItemSupplier obj = null;

      Pair<Integer, Integer> key = new Pair<Integer, Integer>(rs.getInt(column + 0), rs.getInt(column + 1));
      WeakReference<ItemSupplier> ref = knownItemSupplier.get(key);
      if (ref != null) {
         obj = ref.get();
         if (obj != null)
         {
            // I'm not sure if this is necessary, but it's better to be safe
            if (originalItemSupplier.get(obj) == null)
               originalItemSupplier.put(obj, (ItemSupplier)obj.copyForComparison());
            return obj;
         }
      }

      obj = new ItemSupplier(this, rs, column);
      knownItemSupplier.put(obj.idKey(), new WeakReference<ItemSupplier>(obj));
      originalItemSupplier.put(obj, (ItemSupplier)obj.copyForComparison());

      // TODO: this
      return obj;
   }
   
   ItemSupplier createItemSupplier(ResultSet rs, String prefix) throws SQLException
   {
      ItemSupplier obj = null;

      Pair<Integer, Integer> key = new Pair<Integer, Integer>(rs.getInt(prefix + "_ItemId"), rs.getInt(prefix + "_SupplierId"));
      WeakReference<ItemSupplier> ref = knownItemSupplier.get(key);
      if (ref != null) {
         obj = ref.get();
         if (obj != null)
         {
            // I'm not sure if this is necessary, but it's better to be safe
            if (originalItemSupplier.get(obj) == null)
               originalItemSupplier.put(obj, (ItemSupplier)obj.copyForComparison());
            return obj;
         }
      }

      obj = new ItemSupplier(this, rs, prefix);
      knownItemSupplier.put(obj.idKey(), new WeakReference<ItemSupplier>(obj));
      originalItemSupplier.put(obj, (ItemSupplier)obj.copyForComparison());

      // TODO: this
      return obj;
   }
   
   transient HashSet<ItemSupplier> dirtyItemSupplier = new HashSet<ItemSupplier>(); 
   transient WeakHashMap<ItemSupplier, ItemSupplier> originalItemSupplier = new WeakHashMap<ItemSupplier, ItemSupplier>(); 
   transient WeakHashMap<Pair<Integer, Integer>, WeakReference<ItemSupplier>> knownItemSupplier = new WeakHashMap<Pair<Integer, Integer>, WeakReference<ItemSupplier>>();  

   public static class ItemSupplierSQLReader extends EntityReader<ItemSupplier>
   {
      public ItemSupplierSQLReader(EntityManager em)
      {
         super(em, "org/jinq/test/entities/ItemSupplier");
      
         columns.add(new SQLReaderColumnDescription("ItemId", "ItemId", "int"));
      
         columns.add(new SQLReaderColumnDescription("SupplierId", "SupplierId", "int"));
      
      }
      public ItemSupplier readData(ResultSet result, int column)
      {
         try {
            if (em.db.isQueryOnly)
               return new ItemSupplier(em, result, column);
            else
               return em.createItemSupplier(result, column);
         } catch (SQLException e)
         {
            e.printStackTrace();
            return null;
         }
      }
   }

   public class ItemSupplierRowReader implements RowReader <ItemSupplier>
   {
      public int column;
      public String prefix;
      public ItemSupplierRowReader(String prefix)
      {
         this.prefix = prefix;
      }
      protected ItemSupplierRowReader(String prefix, int column)
      {
         this.prefix = prefix;
         this.column = column;
      }
      public ItemSupplier readSqlRow(ResultSet rs)
      {
         try {
            return createItemSupplier(rs, column);
         } catch (SQLException e)
         {
            e.printStackTrace();
         }
         return null;
      }
      public void configureQuery(SelectFromWhere query)
      {
         int firstColumn =  -1;
         int col;

      
         col = query.addSelection(prefix + ".ItemId", prefix + "_ItemId");
         if (firstColumn == -1) firstColumn = col;
      
         col = query.addSelection(prefix + ".SupplierId", prefix + "_SupplierId");
         if (firstColumn == -1) firstColumn = col;
      
         column = firstColumn;
      }
      public RowReader <ItemSupplier> copy()
      {
         return new ItemSupplierRowReader(prefix, column);
      }
      public String queryString()
      {
         return prefix;
      }
   
   }

   
   
      
      
      
      
   
   
   

   // TODO: add plural forms of entity names
   transient SQLQueryComposer<Supplier> supplierQuery;
   transient DBSet<Supplier> supplier;
   public DBSet<Supplier> allSupplier()
   {
      return supplier;
   }
   @EntitySupplier(entityClass="com.example.orm.test.entities.Supplier") 
   public JinqStream<Supplier> supplierStream()
   {
      return new QueryJinqStream<>(supplierQuery);
   }

   void dispose(Supplier obj)
   {
      flushDirty();
      supplier.remove(obj);
      // delete from db immediately
      String sql = "DELETE FROM Suppliers "
         + " WHERE 1=1"
   
         + " AND SupplierId = ? ";
      if (db.testOut != null) db.testOut.println(sql);
      if (db.con != null)
      {
         try {
            PreparedStatement stmt = db.con.prepareStatement(sql);
            int idx = 0;
   
            ++idx;
            stmt.setObject(idx, obj.getSupplierId());
   
            stmt.executeUpdate();
            stmt.close();
         } catch (SQLException e)
         {
            e.printStackTrace();
         }
      }
   }

   void dirtyInstance(Supplier obj)
   {
      dirtySupplier.add(obj);
   }
   
   void newInstance(Supplier obj)
   {
      // TODO: Add handling of objects with auto-generated keys
      // TODO: Add handling of objects whose keys are references to other objects

      assert(obj.em == null);
      obj.em = this;
      // Fix references to other objects that happen to be keys

  
      flushDirty();
      // Add to DB immediately
      supplier.add(obj);

      HashMap<String, Object> map = new HashMap<String, Object>();
      map.put("SupplierId", obj.getSupplierId());
      map.put("Name", obj.getName());
      map.put("Country", obj.getCountry());
      map.put("SupplierId", obj.getSupplierId());
        
      String sql = "INSERT INTO Suppliers (";
      boolean isFirst = true;
      for (Map.Entry entry : map.entrySet())
      {
         if (!isFirst) sql = sql + ", ";
         isFirst = false;
         sql = sql + entry.getKey();
      }
      sql = sql + ") VALUES (";
      for (int n = 0; n < map.size(); n++)
      {
         if (n != 0) sql = sql + ", ";
         sql = sql + "?";
      }
        sql = sql + ")";
        if (db.testOut != null) db.testOut.println(sql);
        if (db.con != null)
        {
         try {
            PreparedStatement stmt = db.con.prepareStatement(sql);
            int idx = 1;
            for (Map.Entry entry : map.entrySet())
            {
               stmt.setObject(idx, entry.getValue());
               idx++;
            }
            stmt.executeUpdate();
            stmt.close();
         } catch (SQLException e)
         {
            e.printStackTrace();
         }
        }
      knownSupplier.put(obj.idKey(), new WeakReference<Supplier>(obj));
      originalSupplier.put(obj, (Supplier)obj.copyForComparison());
   }

   Supplier createSupplier(ResultSet rs, int column) throws SQLException
   {
      Supplier obj = null;

      Integer key = rs.getInt(column + 0);
      WeakReference<Supplier> ref = knownSupplier.get(key);
      if (ref != null) {
         obj = ref.get();
         if (obj != null)
         {
            // I'm not sure if this is necessary, but it's better to be safe
            if (originalSupplier.get(obj) == null)
               originalSupplier.put(obj, (Supplier)obj.copyForComparison());
            return obj;
         }
      }

      obj = new Supplier(this, rs, column);
      knownSupplier.put(obj.idKey(), new WeakReference<Supplier>(obj));
      originalSupplier.put(obj, (Supplier)obj.copyForComparison());

      // TODO: this
      return obj;
   }
   
   Supplier createSupplier(ResultSet rs, String prefix) throws SQLException
   {
      Supplier obj = null;

      Integer key = rs.getInt(prefix + "_SupplierId");
      WeakReference<Supplier> ref = knownSupplier.get(key);
      if (ref != null) {
         obj = ref.get();
         if (obj != null)
         {
            // I'm not sure if this is necessary, but it's better to be safe
            if (originalSupplier.get(obj) == null)
               originalSupplier.put(obj, (Supplier)obj.copyForComparison());
            return obj;
         }
      }

      obj = new Supplier(this, rs, prefix);
      knownSupplier.put(obj.idKey(), new WeakReference<Supplier>(obj));
      originalSupplier.put(obj, (Supplier)obj.copyForComparison());

      // TODO: this
      return obj;
   }
   
   transient HashSet<Supplier> dirtySupplier = new HashSet<Supplier>(); 
   transient WeakHashMap<Supplier, Supplier> originalSupplier = new WeakHashMap<Supplier, Supplier>();   
   transient WeakHashMap<Integer, WeakReference<Supplier>> knownSupplier = new WeakHashMap<Integer, WeakReference<Supplier>>();  

   public static class SupplierSQLReader extends EntityReader<Supplier>
   {
      public SupplierSQLReader(EntityManager em)
      {
         super(em, "org/jinq/test/entities/Supplier");
      
         columns.add(new SQLReaderColumnDescription("SupplierId", "SupplierId", "int"));
      
         columns.add(new SQLReaderColumnDescription("Name", "Name", "String"));
      
         columns.add(new SQLReaderColumnDescription("Country", "Country", "String"));
      
      }
      public Supplier readData(ResultSet result, int column)
      {
         try {
            if (em.db.isQueryOnly)
               return new Supplier(em, result, column);
            else
               return em.createSupplier(result, column);
         } catch (SQLException e)
         {
            e.printStackTrace();
            return null;
         }
      }
   }

   public class SupplierRowReader implements RowReader <Supplier>
   {
      public int column;
      public String prefix;
      public SupplierRowReader(String prefix)
      {
         this.prefix = prefix;
      }
      protected SupplierRowReader(String prefix, int column)
      {
         this.prefix = prefix;
         this.column = column;
      }
      public Supplier readSqlRow(ResultSet rs)
      {
         try {
            return createSupplier(rs, column);
         } catch (SQLException e)
         {
            e.printStackTrace();
         }
         return null;
      }
      public void configureQuery(SelectFromWhere query)
      {
         int firstColumn =  -1;
         int col;

      
         col = query.addSelection(prefix + ".SupplierId", prefix + "_SupplierId");
         if (firstColumn == -1) firstColumn = col;
      
         col = query.addSelection(prefix + ".Name", prefix + "_Name");
         if (firstColumn == -1) firstColumn = col;
      
         col = query.addSelection(prefix + ".Country", prefix + "_Country");
         if (firstColumn == -1) firstColumn = col;
      
         column = firstColumn;
      }
      public RowReader <Supplier> copy()
      {
         return new SupplierRowReader(prefix, column);
      }
      public String queryString()
      {
         return prefix;
      }
   

      String SuppliesPrefix;
      public String materializeSuppliesJoinString(SelectFromWhere query)
      {
         if (SuppliesPrefix == null)
         {
         
            String middlePrefix = query.addTable("ItemSuppliers");
         SuppliesPrefix = query.addTable("Items");
         
            query.addWhereClause(SuppliesPrefix + ".ItemId = " + middlePrefix + ".ItemId");
         
            query.addWhereClause(middlePrefix + ".SupplierId = " + prefix + ".SupplierId");
         
         }
         return SuppliesPrefix;
      }
      public ItemRowReader materializeSuppliesJoinRowReader(SelectFromWhere query)
      {
         return new ItemRowReader(materializeSuppliesJoinString(query));
      }
   
   }



   public SQLReader getReaderForEntity(String entity)
   {
      
      if ("Customer".equals(entity))
      {
         return new CustomerSQLReader(this);       
      }
      
      if ("Sale".equals(entity))
      {
         return new SaleSQLReader(this);        
      }
      
      if ("LineOrder".equals(entity))
      {
         return new LineOrderSQLReader(this);         
      }
      
      if ("Item".equals(entity))
      {
         return new ItemSQLReader(this);        
      }
      
      if ("ItemSupplier".equals(entity))
      {
         return new ItemSupplierSQLReader(this);         
      }
      
      if ("Supplier".equals(entity))
      {
         return new SupplierSQLReader(this);       
      }
      
      return null;
   }
   public String[] getEntityColumnNames(String entity)
   {
      
      if ("Customer".equals(entity))
      {
         return new CustomerSQLReader(this).getColumnNames();        
      }
      
      if ("Sale".equals(entity))
      {
         return new SaleSQLReader(this).getColumnNames();         
      }
      
      if ("LineOrder".equals(entity))
      {
         return new LineOrderSQLReader(this).getColumnNames();       
      }
      
      if ("Item".equals(entity))
      {
         return new ItemSQLReader(this).getColumnNames();         
      }
      
      if ("ItemSupplier".equals(entity))
      {
         return new ItemSupplierSQLReader(this).getColumnNames();       
      }
      
      if ("Supplier".equals(entity))
      {
         return new SupplierSQLReader(this).getColumnNames();        
      }
      
      return null;
   }
   public String getTableForEntity(String entity)
   {
      
      if ("Customer".equals(entity))
      {
         return "Customers"; 
      }
      
      if ("Sale".equals(entity))
      {
         return "Sales"; 
      }
      
      if ("LineOrder".equals(entity))
      {
         return "LineOrders"; 
      }
      
      if ("Item".equals(entity))
      {
         return "Items"; 
      }
      
      if ("ItemSupplier".equals(entity))
      {
         return "ItemSuppliers"; 
      }
      
      if ("Supplier".equals(entity))
      {
         return "Suppliers"; 
      }
      
      return null;
   }
   
   static class QueryCacheKey
   {
      QueryCacheKey(String context, Object baseQuery, Object lambda)
      {
         this.context = context;
         this.baseQuery = baseQuery;
         this.lambda = lambda;
      }
      public int hashCode()
      {
         return context.hashCode() ^ baseQuery.hashCode() ^ lambda.hashCode();
      }
      public boolean equals(Object o)
      {
         if (!(o instanceof QueryCacheKey)) return false;
         QueryCacheKey other = (QueryCacheKey)o;
         return other.context.equals(context)
            && other.baseQuery.equals(baseQuery)
            && other.lambda.equals(lambda);
      }
      String context;
      Object baseQuery;
      Object lambda;
   }
   static class QueryCacheValue
   {
      QueryCacheValue(QueryCacheKey key, Object cachedQuery)
      {
         this.key = key;
         this.cachedQuery = cachedQuery;
      }
      QueryCacheKey key;
      Object cachedQuery;
      QueryCacheValue prev = null;
      QueryCacheValue next = null;
   }
   transient Map<QueryCacheKey, QueryCacheValue> queryCache = new HashMap<QueryCacheKey, QueryCacheValue>();
   final static int QUERY_CACHE_LIMIT = 500;
   transient QueryCacheValue queryCacheLRU_head = null;
   transient QueryCacheValue queryCacheLRU_tail = null;
   int queryCacheSize = 0;
   private void dequeueQueryCacheLRU(QueryCacheValue val)
   {
      queryCacheSize--;
      if (val.next == null)
         queryCacheLRU_tail = val.prev;
      else
         val.next.prev = val.prev;
      if (val.prev == null)
         queryCacheLRU_head = val.next;
      else
         val.prev.next = val.next;
      val.prev = null;
      val.next = null;
   }
   private void enqueueQueryCacheLRU(QueryCacheValue val)
   {
      // Add it to the end of the queue
      if (queryCacheLRU_tail == null)
      {
         queryCacheLRU_tail = val;
         queryCacheLRU_head = val;
      }
      else
      {
         val.prev = queryCacheLRU_tail;
         queryCacheLRU_tail.next = val;
         queryCacheLRU_tail = val;
      }
      // Check if we've overflowed
      queryCacheSize++;
      if (queryCacheSize > QUERY_CACHE_LIMIT)
      {
         QueryCacheValue toRemove = queryCacheLRU_head;
         dequeueQueryCacheLRU(toRemove);
         queryCache.remove(toRemove.key);
      }
   }
   public Object getQueryCacheEntry(String context, Object baseQuery, Object lambda)
   {
      QueryCacheKey key = new QueryCacheKey(context, baseQuery, lambda);
      QueryCacheValue cached = queryCache.get(key);
      if (cached == null)
         return null;
      // Move it to the end of the queue
      dequeueQueryCacheLRU(cached);
      enqueueQueryCacheLRU(cached);
      return cached.cachedQuery;
   }
   public void putQueryCacheEntry(String context, Object baseQuery, Object lambda, Object cachedQuery)
   {
      QueryCacheKey key = new QueryCacheKey(context, baseQuery, lambda);
      QueryCacheValue cached = new QueryCacheValue(key, cachedQuery);
      queryCache.put(key, cached);
      enqueueQueryCacheLRU(cached);
   }
   static class GeneratedCachedQuery
   {
      GeneratedCachedQuery(Object key, Object cachedQuery)
      {
         this.key = key;
         this.cachedQuery = cachedQuery;
      }
      Object key;
      Object cachedQuery;
      GeneratedCachedQuery prev = null;
      GeneratedCachedQuery next = null;
   }
   transient Map<Object, GeneratedCachedQuery> generatedQueryCache = new HashMap<Object, GeneratedCachedQuery>();
   final static int GENERATED_QUERY_CACHE_LIMIT = 500;
   transient GeneratedCachedQuery generatedQueryCacheLRU_head = null;
   transient GeneratedCachedQuery generatedQueryCacheLRU_tail = null;
   int generatedQueryCacheSize = 0;
   private void dequeueGeneratedQueryCacheLRU(GeneratedCachedQuery val)
   {
      generatedQueryCacheSize--;
      if (val.next == null)
         generatedQueryCacheLRU_tail = val.prev;
      else
         val.next.prev = val.prev;
      if (val.prev == null)
         generatedQueryCacheLRU_head = val.next;
      else
         val.prev.next = val.next;
      val.prev = null;
      val.next = null;
   }
   private void enqueueGeneratedQueryCacheLRU(GeneratedCachedQuery val)
   {
      // Add it to the end of the queue
      if (generatedQueryCacheLRU_tail == null)
      {
         generatedQueryCacheLRU_tail = val;
         generatedQueryCacheLRU_head = val;
      }
      else
      {
         val.prev = generatedQueryCacheLRU_tail;
         generatedQueryCacheLRU_tail.next = val;
         generatedQueryCacheLRU_tail = val;
      }
      // Check if we've overflowed
      generatedQueryCacheSize++;
      if (generatedQueryCacheSize > GENERATED_QUERY_CACHE_LIMIT)
      {
         GeneratedCachedQuery toRemove = generatedQueryCacheLRU_head;
         dequeueGeneratedQueryCacheLRU(toRemove);
         generatedQueryCache.remove(toRemove.key);
      }
   }
   public Object getGeneratedQueryCacheEntry(Object queryRepresentation)
   {
      GeneratedCachedQuery cached = generatedQueryCache.get(queryRepresentation);
      if (cached == null)
         return null;
      // Move it to the end of the queue
      dequeueGeneratedQueryCacheLRU(cached);
      enqueueGeneratedQueryCacheLRU(cached);
      return cached.cachedQuery;
   }
   public void putGeneratedQueryCacheEntry(Object queryRepresentation, Object generatedQuery)
   {
      GeneratedCachedQuery cached = new GeneratedCachedQuery(queryRepresentation, generatedQuery);
      generatedQueryCache.put(queryRepresentation, cached);
      enqueueGeneratedQueryCacheLRU(cached);
   }
   
   public boolean isQueriesCached()
   {
      return cacheQueries || partialCacheQueries;
   }
   
   public boolean isPreparedStatementCached()
   {
         return cacheQueries;
   }
   
}
