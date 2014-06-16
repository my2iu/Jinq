
package org.jinq.test.entities;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.lang.reflect.Method;
import java.io.PrintWriter;

import ch.epfl.labos.iu.orm.query2.SQLQueryTransforms;
import ch.epfl.labos.iu.orm.queryll2.runtime.QueryllEntityConfigurationInfo;
import ch.epfl.labos.iu.orm.queryll2.runtime.ConfigureQueryll;
import ch.epfl.labos.iu.orm.queryll2.runtime.ORMEntity;
import ch.epfl.labos.iu.orm.queryll2.runtime.ORMField;

public class DBManager implements ConfigureQueryll
{
	public PrintWriter testOut = null;  // When non-null, we're in test mode
	public Connection con = null;
	public boolean isQueryOnly = false;
	public boolean usePartialQueryCaching = true;
	public boolean useFullQueryCaching = true;

	EntityManager cachedEntityManager;

	public EntityManager begin()
	{
		if (con != null)
		{
			if (!isQueryOnly)
			{
				try {
					con.setAutoCommit(false);
				} catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}
		if (isQueryOnly && cachedEntityManager != null)
		{
			EntityManager toReturn = cachedEntityManager;
			cachedEntityManager = null;
			return toReturn;
		} 
		return new EntityManager(this);

	}
	
	public void end(EntityManager em, boolean commit)
	{
		try {
			if (!isQueryOnly)
			{
				if (commit)
				{
					em.flushDirty();
					if (con != null) con.commit();
				}
				else
				{
					if (con != null) con.rollback();
				}
				if (con != null) con.setAutoCommit(true);
			}
			else
				cachedEntityManager = em;
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void doTransaction(Transaction transaction)
	{
		EntityManager em = begin();
		end(em, transaction.execute(em));
	}

	public static interface Transaction
	{
		// returns true to commit the changes
		public boolean execute(EntityManager em);
	}

	public void close()
	{
		try {
			if (con != null) con.close();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}


	

	
		
		
		
		
		
	
	
	
		
		
		
	
	
	
		
		
	
	
	
		
		
		
	
	
	
		
		  
		  
		
	

	
		
		
		
	

	
		
		
		
		
	
	
	
		
		
	
	
	
		
		
		
		
	
	
	
		
		
		
	


	public void configureQueryll(QueryllEntityConfigurationInfo config)
	{
		
		config.registerORMEntity(new ORMEntity(
			"org.jinq.test.entities",
			"Customer",
			"Customers",
			new ORMField[] {
				
				new ORMField("CustomerId",
					"int",
					"CustomerId",
					true,
					false
					),

				new ORMField("Name",
					"String",
					"Name",
					false,
					false
					),

				new ORMField("Country",
					"String",
					"Country",
					false,
					false
					),

				new ORMField("Debt",
					"int",
					"Debt",
					false,
					false
					),

				new ORMField("Salary",
					"int",
					"Salary",
					false,
					false
					),
			
				}));

		config.registerORMEntity(new ORMEntity(
			"org.jinq.test.entities",
			"Sale",
			"Sales",
			new ORMField[] {
				
				new ORMField("SaleId",
					"int",
					"SaleId",
					true,
					false
					),

				new ORMField("Date",
					"String",
					"Date",
					false,
					false
					),
			
				}));

		config.registerORMEntity(new ORMEntity(
			"org.jinq.test.entities",
			"LineOrder",
			"LineOrders",
			new ORMField[] {
				
				new ORMField("SaleId",
					"int",
					"SaleId",
					true,
					true
					),

				new ORMField("ItemId",
					"int",
					"ItemId",
					true,
					true
					),

				new ORMField("Quantity",
					"int",
					"Quantity",
					false,
					false
					),
			
				}));

		config.registerORMEntity(new ORMEntity(
			"org.jinq.test.entities",
			"Item",
			"Items",
			new ORMField[] {
				
				new ORMField("ItemId",
					"int",
					"ItemId",
					true,
					false
					),

				new ORMField("Name",
					"String",
					"Name",
					false,
					false
					),

				new ORMField("SalePrice",
					"double",
					"SalePrice",
					false,
					false
					),

				new ORMField("PurchasePrice",
					"double",
					"PurchasePrice",
					false,
					false
					),
			
				}));

		config.registerORMEntity(new ORMEntity(
			"org.jinq.test.entities",
			"ItemSupplier",
			"ItemSuppliers",
			new ORMField[] {
				
				new ORMField("ItemId",
					"int",
					"ItemId",
					true,
					false
					),

				new ORMField("SupplierId",
					"int",
					"SupplierId",
					true,
					false
					),
			
				}));

		config.registerORMEntity(new ORMEntity(
			"org.jinq.test.entities",
			"Supplier",
			"Suppliers",
			new ORMField[] {
				
				new ORMField("SupplierId",
					"int",
					"SupplierId",
					true,
					false
					),

				new ORMField("Name",
					"String",
					"Name",
					false,
					false
					),

				new ORMField("Country",
					"String",
					"Country",
					false,
					false
					),
			
				}));

		config.registerORMSimpleLink(
			"org.jinq.test.entities",
			"1:N",
			"Customer",
			"Purchases",
			"CustomerId",
			"Sale",
			"Purchaser",
			"CustomerId");

		config.registerORMSimpleLink(
			"org.jinq.test.entities",
			"1:N",
			"Sale",
			"SaleLine",
			"SaleId",
			"LineOrder",
			"Sale",
			"SaleId");

		config.registerORMSimpleLink(
			"org.jinq.test.entities",
			"N:1",
			"LineOrder",
			"Item",
			"ItemId",
			"Item",
			"Orders",
			"ItemId");

		config.registerORMNMLink(
			"org.jinq.test.entities",
			"Item",
			"Suppliers",
			"ItemId",
			"ItemSuppliers",
			"ItemId",
			"SupplierId",
			"Supplier",
			"Supplies",
			"SupplierId");

	}
	SQLQueryTransforms queryll;  // Place for storing opaque queryll information
	public void storeQueryllAnalysisResults(SQLQueryTransforms queryllAnalysis)
	{
		this.queryll = queryllAnalysis;
	}

	// Creates a debug DBManager that displays queries directly to output 
	// and does not start or query a real database
	// 
	public DBManager(PrintWriter output)
	{
		this(output, null, null, false);
	}

	public DBManager(boolean logQueries)
	{
		this(null, "org.apache.derby.jdbc.EmbeddedDriver", "jdbc:derby:demoDB", logQueries); 
	}

	public DBManager()
	{
		this(true);
	}

	public DBManager(PrintWriter output, String jdbcDriver, String jdbcPath, boolean logQueries)
	{
		testOut = output;
		if (jdbcPath != null)
		{
			try {
				if (jdbcDriver != null)
					Class.forName(jdbcDriver);
				con = DriverManager.getConnection(
				      jdbcPath/*, "APP", "APP"*/);
				
				if (logQueries)
				{
					con = (Connection)Class.forName("ch.epfl.labos.iu.orm.trace.LoggedConnection")
						.getConstructor(Class.forName("java.sql.Connection"))
						.newInstance(con);
				}
			} catch(Exception e)
			{
				e.printStackTrace();
			}
	        try
	        {
	           Method m = java.lang.Class.forName("org.jinq.test.entities.OptimizationBackdoor").getMethod("initializeOptimizations", new Class[0]);
	           m.invoke(null, new Object[0]);
	        } catch (Exception e) {}
		}
	}

	
	public int timeout = 0;
}
