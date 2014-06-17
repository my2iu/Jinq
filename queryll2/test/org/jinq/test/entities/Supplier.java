
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

public class Supplier implements Cloneable
{
	EntityManager em;

	public Supplier()
	{

supplies = new SuppliesSet(true);

	}


		
	
	private int supplierId;
	public int getSupplierId()
	{
		return supplierId;
	}
	public void setSupplierId(int _val)
	{
		if (em != null)
			em.dirtyInstance(this);
		supplierId = _val;
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

	

	class SuppliesSet extends LazySet<Item>
	{
		protected VectorSet<Item> createRealizedSet()
		{
			VectorSet<Item> newset = new VectorSet<Item>();
			try {
				PreparedStatement stmt = em.db.con.prepareStatement("SELECT "
					+ " 1, A.SupplierId as A_SupplierId, A.Name as A_Name, A.Country as A_Country, A.SupplierId as LINK_A_SupplierId"
					+ ", 1, C.ItemId as C_ItemId, C.Name as C_Name, C.SalePrice as C_SalePrice, C.PurchasePrice as C_PurchasePrice, C.ItemId as LINK_C_ItemId, C.ItemId as LINK_C_ItemId"
					+ " FROM Suppliers as A, Items AS C, ItemSuppliers as B "
					+ " WHERE 1=1"
	
					+ " AND A.SupplierId = ? "
	
					+ " AND B.ItemId = C.ItemId"
	
					+ " AND A.SupplierId = B.SupplierId"
	
					);
				int idx = 0;
	
				idx++;
				stmt.setObject(idx, supplierId);
	
				ResultSet rs = stmt.executeQuery();
				while ( rs.next() ) {
					newset.add(em.createItem(rs, "C"));
				}
				rs.close();
				stmt.close();
			} catch(SQLException e)
			{
				e.printStackTrace();
			}
		
			return newset;
		}	
		public SuppliesSet(boolean isEmpty)
		{
			super(isEmpty);
		}
		public boolean add(Item _val)
		{
			if (em != null)
				em.dirtyInstance(Supplier.this);
			return super.add(_val);
		}
		public boolean remove(Item _val)
		{
			if (em != null)
				em.dirtyInstance(Supplier.this);
			return super.remove(_val);
		}
	}
	DBSet<Item> supplies;
	public DBSet<Item> getSupplies()
	{
		return supplies;
	}
	public void setSupplies(DBSet<Item> _val)
	{
		supplies = _val;
	}


	public void persist(EntityManager em)
	{
		this.em = em;
		idKey = supplierId;
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


		for (Item obj: getSupplies())
			obj.getSuppliers().remove(this);

		if (em != null)
			em.dispose(this);
		em = null;
	}
	
	public Supplier(EntityManager em, ResultSet rs, int column) throws SQLException
	{
		this.em = em;

		supplierId = rs.getInt(column);
		column++;

		name = rs.getString(column);
		column++;

		country = rs.getString(column);
		column++;

	supplies = new SuppliesSet(false);

		idKey = supplierId;
	}

	public Supplier(EntityManager em, ResultSet rs, String prefix) throws SQLException
	{
		this.em = em;

		supplierId = rs.getInt(prefix + "_SupplierId");

		name = rs.getString(prefix + "_Name");

		country = rs.getString(prefix + "_Country");

	supplies = new SuppliesSet(false);

		idKey = supplierId;
	}

	Integer idKey;
	public Integer idKey()
	{
		return idKey;
	}
	
	Supplier comparisonCopy;
	public Supplier copyForComparison()
	{
		if (comparisonCopy != null) return comparisonCopy;
		Supplier copy = new Supplier();

		copy.supplierId = supplierId;

		copy.name = name;

		copy.country = country;


		copy.supplies = supplies.comparisonClone();
		if (supplies instanceof LazySet)
		{
			((LazySet<Item>)supplies).setRealizeListener((LazySet<Item>)copy.supplies);
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
