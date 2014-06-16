
package org.jinq.test.entities;

import ch.epfl.labos.iu.orm.DBSet;
import ch.epfl.labos.iu.orm.VectorSet;
import ch.epfl.labos.iu.orm.LazySet;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import ch.epfl.labos.iu.orm.Triple;
import ch.epfl.labos.iu.orm.Quartic;
import ch.epfl.labos.iu.orm.Quintic;

public class Customer implements Cloneable
{
	EntityManager em;

	public Customer()
	{

purchases = new PurchasesSet(true);

	}


		
	
	private int customerId;
	public int getCustomerId()
	{
		return customerId;
	}
	public void setCustomerId(int _val)
	{
		if (em != null)
			em.dirtyInstance(this);
		customerId = _val;
	}

		
	
	private String name;
	public String getName()
	{
		return name;
	}
	public void setName(String _val)
	{
		if (em != null)
			em.dirtyInstance(this);
		name = _val;
	}

		
	
	private String country;
	public String getCountry()
	{
		return country;
	}
	public void setCountry(String _val)
	{
		if (em != null)
			em.dirtyInstance(this);
		country = _val;
	}

		
	
	private int debt;
	public int getDebt()
	{
		return debt;
	}
	public void setDebt(int _val)
	{
		if (em != null)
			em.dirtyInstance(this);
		debt = _val;
	}

		
	
	private int salary;
	public int getSalary()
	{
		return salary;
	}
	public void setSalary(int _val)
	{
		if (em != null)
			em.dirtyInstance(this);
		salary = _val;
	}

	

	class PurchasesSet extends LazySet<Sale>
	{
		protected VectorSet<Sale> createRealizedSet()
		{
			VectorSet<Sale> newset = new VectorSet<Sale>();
			try {
				PreparedStatement stmt = em.db.con.prepareStatement("SELECT "
					+ " 1, A.CustomerId as A_CustomerId, A.Name as A_Name, A.Country as A_Country, A.Debt as A_Debt, A.Salary as A_Salary, A.CustomerId as LINK_A_CustomerId"
					+ ", 1, C.SaleId as C_SaleId, C.Date as C_Date, C.SaleId as LINK_C_SaleId, C.CustomerId as LINK_C_CustomerId"
					+ " FROM Customers as A, Sales AS C"
					+ " WHERE 1=1"
	
					+ " AND A.CustomerId = ?"
	
					+ " AND A.CustomerId = C.CustomerId"
	
					);
				int idx = 0;
	
				idx++;
				stmt.setObject(idx, customerId);
	

				ResultSet rs = stmt.executeQuery();
				while ( rs.next() ) {
					newset.add(em.createSale(rs, "C"));
				}
				rs.close();
				stmt.close();
			} catch(SQLException e)
			{
				e.printStackTrace();
			}
		
			return newset;
		}	
		public PurchasesSet(boolean isEmpty)
		{
			super(isEmpty);
		}
		public boolean add(Sale _val)
		{
			super.add(_val);
			if (em != null)
				em.dirtyInstance(Customer.this);
			_val.setPurchaser(Customer.this);
			return true;
		}
		public boolean remove(Sale _val)
		{
			super.remove(_val);
			if (em != null)
				em.dirtyInstance(Customer.this);
			_val.setPurchaser(null);
			return true;
		}
	}
	DBSet<Sale> purchases;
	public DBSet<Sale> getPurchases()
	{
		return purchases;
	}
	public void setPurchases(DBSet<Sale> _val)
	{
		purchases = _val;
	}


	public void persist(EntityManager em)
	{
		this.em = em;
		idKey = customerId;
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


		getPurchases().clear();

		if (em != null)
			em.dispose(this);
		em = null;
	}
	
	public Customer(EntityManager em, ResultSet rs, int column) throws SQLException
	{
		this.em = em;

		customerId = rs.getInt(column);
		column++;

		name = rs.getString(column);
		column++;

		country = rs.getString(column);
		column++;

		debt = rs.getInt(column);
		column++;

		salary = rs.getInt(column);
		column++;

	purchases = new PurchasesSet(false);

		idKey = customerId;
	}

	public Customer(EntityManager em, ResultSet rs, String prefix) throws SQLException
	{
		this.em = em;

		customerId = rs.getInt(prefix + "_CustomerId");

		name = rs.getString(prefix + "_Name");

		country = rs.getString(prefix + "_Country");

		debt = rs.getInt(prefix + "_Debt");

		salary = rs.getInt(prefix + "_Salary");

	purchases = new PurchasesSet(false);

		idKey = customerId;
	}

	Integer idKey;
	public Integer idKey()
	{
		return idKey;
	}
	
	Customer comparisonCopy;
	public Customer copyForComparison()
	{
		if (comparisonCopy != null) return comparisonCopy;
		Customer copy = new Customer();

		copy.customerId = customerId;

		copy.name = name;

		copy.country = country;

		copy.debt = debt;

		copy.salary = salary;


		copy.purchases = purchases.comparisonClone();
		if (purchases instanceof LazySet && copy.purchases instanceof LazySet)
		{
			((LazySet<Sale>)purchases).setRealizeListener((LazySet<Sale>)copy.purchases);
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
