
package org.jinq.test.entities;

import ch.epfl.labos.iu.orm.DBSet;
import ch.epfl.labos.iu.orm.VectorSet;
import ch.epfl.labos.iu.orm.LazySet;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.jinq.tuples.Pair;

import ch.epfl.labos.iu.orm.Triple;
import ch.epfl.labos.iu.orm.Quartic;
import ch.epfl.labos.iu.orm.Quintic;

public class Sale implements Cloneable
{
	EntityManager em;

	public Sale()
	{

saleLine = new SaleLineSet(true);


		isPurchaserRealized = true;

	}


		
	
	private int saleId;
	public int getSaleId()
	{
		return saleId;
	}
	public void setSaleId(int _val)
	{
		if (em != null)
			em.dirtyInstance(this);
		saleId = _val;
	}

		
	
	private String date;
	public String getDate()
	{
		return date;
	}
	public void setDate(String _val)
	{
		if (em != null)
			em.dirtyInstance(this);
		date = _val;
	}

	

	class SaleLineSet extends LazySet<LineOrder>
	{
		protected VectorSet<LineOrder> createRealizedSet()
		{
			VectorSet<LineOrder> newset = new VectorSet<LineOrder>();
			try {
				PreparedStatement stmt = em.db.con.prepareStatement("SELECT "
					+ " 1, A.SaleId as A_SaleId, A.Date as A_Date, A.SaleId as LINK_A_SaleId, A.CustomerId as LINK_A_CustomerId"
					+ ", 1, C.SaleId as C_SaleId, C.ItemId as C_ItemId, C.Quantity as C_Quantity, C.ItemId as LINK_C_ItemId, C.SaleId as LINK_C_SaleId"
					+ " FROM Sales as A, LineOrders AS C"
					+ " WHERE 1=1"
	
					+ " AND A.SaleId = ?"
	
					+ " AND A.SaleId = C.SaleId"
	
					);
				int idx = 0;
	
				idx++;
				stmt.setObject(idx, saleId);
	

				ResultSet rs = stmt.executeQuery();
				while ( rs.next() ) {
					newset.add(em.createLineOrder(rs, "C"));
				}
				rs.close();
				stmt.close();
			} catch(SQLException e)
			{
				e.printStackTrace();
			}
		
			return newset;
		}	
		public SaleLineSet(boolean isEmpty)
		{
			super(isEmpty);
		}
		public boolean add(LineOrder _val)
		{
			super.add(_val);
			if (em != null)
				em.dirtyInstance(Sale.this);
			_val.setSale(Sale.this);
			return true;
		}
		public boolean remove(LineOrder _val)
		{
			super.remove(_val);
			if (em != null)
				em.dirtyInstance(Sale.this);
			_val.setSale(null);
			return true;
		}
	}
	DBSet<LineOrder> saleLine;
	public DBSet<LineOrder> getSaleLine()
	{
		return saleLine;
	}
	public void setSaleLine(DBSet<LineOrder> _val)
	{
		saleLine = _val;
	}


	boolean isPurchaserRealized;
	String _linkcol_purchaser_CustomerId;
	Customer purchaser;
	public Customer getPurchaser()
	{
		if (!isPurchaserRealized)
		{
			try {
				PreparedStatement stmt = em.db.con.prepareStatement("SELECT "
					+ " 1, C.CustomerId as C_CustomerId, C.Name as C_Name, C.Country as C_Country, C.Debt as C_Debt, C.Salary as C_Salary, C.CustomerId as LINK_C_CustomerId"
					+ " FROM Customers as C"
					+ " WHERE 1=1"
	
					+ " AND C.CustomerId = " + _linkcol_purchaser_CustomerId
					);
				ResultSet rs = stmt.executeQuery();
				if (rs.next())
					purchaser = em.createCustomer(rs, "C");
				rs.close();
				stmt.close();
				isPurchaserRealized = true;
			} catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
		return purchaser;
	}
	public void setPurchaser(Customer _val)
	{
		if (em != null)
			em.dirtyInstance(this);
		purchaser = _val;
		isPurchaserRealized = true;
	}


	public void persist(EntityManager em)
	{
		this.em = em;
		idKey = saleId;
		em.newInstance(this);
	}

	public void markAsDirty()
	{
		if (em != null)
		{
			em.dirtyInstance(this);
			// TODO: extend this to sets
		}
	}

	public void dispose()
	{


		getSaleLine().clear();


		if (getPurchaser() != null)
			getPurchaser().getPurchases().remove(this);

		if (em != null)
			em.dispose(this);
		em = null;
	}
	
	public Sale(EntityManager em, ResultSet rs, int column) throws SQLException
	{
		this.em = em;

		saleId = rs.getInt(column);
		column++;

		date = rs.getString(column);
		column++;

	saleLine = new SaleLineSet(false);


		isPurchaserRealized = false;
		_linkcol_purchaser_CustomerId = rs.getString(column);
		column++;
		
		idKey = saleId;
	}

	public Sale(EntityManager em, ResultSet rs, String prefix) throws SQLException
	{
		this.em = em;

		saleId = rs.getInt(prefix + "_SaleId");

		date = rs.getString(prefix + "_Date");

	saleLine = new SaleLineSet(false);


		isPurchaserRealized = false;
		_linkcol_purchaser_CustomerId = rs.getString("LINK_" + prefix + "_CustomerId");
		
		idKey = saleId;
	}

	Integer idKey;
	public Integer idKey()
	{
		return idKey;
	}
	
	Sale comparisonCopy;
	public Sale copyForComparison()
	{
		if (comparisonCopy != null) return comparisonCopy;
		Sale copy = new Sale();

		copy.saleId = saleId;

		copy.date = date;


		copy.saleLine = saleLine.comparisonClone();
		if (saleLine instanceof LazySet && copy.saleLine instanceof LazySet)
		{
			((LazySet<LineOrder>)saleLine).setRealizeListener((LazySet<LineOrder>)copy.saleLine);
		}

		
		
		
	
		comparisonCopy = copy;
		return copy;
	}
	
	public Object clone() throws CloneNotSupportedException
	{	
		// TODO: This is bogus (doesn't handle sets correctly)
		return super.clone();
	}
}
