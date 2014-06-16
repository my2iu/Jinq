
package org.jinq.test.entities;

import ch.epfl.labos.iu.orm.DBSet;
import ch.epfl.labos.iu.orm.VectorSet;
import ch.epfl.labos.iu.orm.LazySet;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import ch.epfl.labos.iu.orm.Pair;
import ch.epfl.labos.iu.orm.Triple;
import ch.epfl.labos.iu.orm.Quartic;
import ch.epfl.labos.iu.orm.Quintic;

public class Item implements Cloneable
{
	EntityManager em;

	public Item()
	{

suppliers = new SuppliersSet(true);
orders = new OrdersSet(true);

	}


		
	
	private int itemId;
	public int getItemId()
	{
		return itemId;
	}
	public void setItemId(int _val)
	{
		if (em != null)
			em.dirtyInstance(this);
		itemId = _val;
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

		
	
	private double salePrice;
	public double getSalePrice()
	{
		return salePrice;
	}
	public void setSalePrice(double _val)
	{
		if (em != null)
			em.dirtyInstance(this);
		salePrice = _val;
	}

		
	
	private double purchasePrice;
	public double getPurchasePrice()
	{
		return purchasePrice;
	}
	public void setPurchasePrice(double _val)
	{
		if (em != null)
			em.dirtyInstance(this);
		purchasePrice = _val;
	}

	

	class SuppliersSet extends LazySet<Supplier>
	{
		protected VectorSet<Supplier> createRealizedSet()
		{
			VectorSet<Supplier> newset = new VectorSet<Supplier>();
			try {
				PreparedStatement stmt = em.db.con.prepareStatement("SELECT "
					+ " 1, A.ItemId as A_ItemId, A.Name as A_Name, A.SalePrice as A_SalePrice, A.PurchasePrice as A_PurchasePrice, A.ItemId as LINK_A_ItemId, A.ItemId as LINK_A_ItemId"
					+ ", 1, C.SupplierId as C_SupplierId, C.Name as C_Name, C.Country as C_Country, C.SupplierId as LINK_C_SupplierId"
					+ " FROM Items as A, Suppliers AS C, ItemSuppliers as B"
					+ " WHERE 1=1"
	
					+ " AND A.ItemId = ? "
	
					+ " AND A.ItemId = B.ItemId"
	
					+ " AND B.SupplierId = C.SupplierId"
	
					);
				int idx = 0;
	
				idx++;
				stmt.setObject(idx, itemId);
	
				ResultSet rs = stmt.executeQuery();
				while ( rs.next() ) {
					newset.add(em.createSupplier(rs, "C"));
				}
				rs.close();
				stmt.close();
			} catch(SQLException e)
			{
				e.printStackTrace();
			}
		
			return newset;
		}	
		public SuppliersSet(boolean isEmpty)
		{
			super(isEmpty);
		}
		public boolean add(Supplier _val)
		{
			if (em != null)
				em.dirtyInstance(Item.this);
			super.add(_val);
			_val.getSupplies().add(Item.this);
			return true;
		}
		public boolean remove(Supplier _val)
		{
			if (em != null)
				em.dirtyInstance(Item.this);
			super.remove(_val);
			_val.getSupplies().remove(Item.this);
			return true;
		}
	}
	DBSet<Supplier> suppliers;
	public DBSet<Supplier> getSuppliers()
	{
		return suppliers;
	}
	public void setSuppliers(DBSet<Supplier> _val)
	{
		suppliers = _val;
	}


	class OrdersSet extends LazySet<LineOrder>
	{
		protected VectorSet<LineOrder> createRealizedSet()
		{
			VectorSet<LineOrder> newset = new VectorSet<LineOrder>();
			try {
				PreparedStatement stmt = em.db.con.prepareStatement("SELECT "
					+ " 1, A.ItemId as A_ItemId, A.Name as A_Name, A.SalePrice as A_SalePrice, A.PurchasePrice as A_PurchasePrice, A.ItemId as LINK_A_ItemId, A.ItemId as LINK_A_ItemId"
					+ ", 1, C.SaleId as C_SaleId, C.ItemId as C_ItemId, C.Quantity as C_Quantity, C.ItemId as LINK_C_ItemId, C.SaleId as LINK_C_SaleId"
					+ " FROM Items as A, LineOrders AS C"
					+ " WHERE 1=1"
	
					+ " AND A.ItemId = ? "
	
					+ " AND A.ItemId = C.ItemId"
	
					);
				int idx = 0;
	
				idx++;
				stmt.setObject(idx, itemId);
	

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
		public OrdersSet(boolean isEmpty)
		{
			super(isEmpty);
		}
		public boolean add(LineOrder _val)
		{
			if (em != null)
				em.dirtyInstance(Item.this);
			return super.add(_val);
		}
		public boolean remove(LineOrder _val)
		{
			if (em != null)
				em.dirtyInstance(Item.this);
			return super.remove(_val);
		}
	}
	DBSet<LineOrder> orders;
	public DBSet<LineOrder> getOrders()
	{
		return orders;
	}
	public void setOrders(DBSet<LineOrder> _val)
	{
		orders = _val;
	}


	public void persist(EntityManager em)
	{
		this.em = em;
		idKey = itemId;
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


		getSuppliers().clear();


		for (LineOrder obj: getOrders())
			obj.setItem(null);

		if (em != null)
			em.dispose(this);
		em = null;
	}
	
	public Item(EntityManager em, ResultSet rs, int column) throws SQLException
	{
		this.em = em;

		itemId = rs.getInt(column);
		column++;

		name = rs.getString(column);
		column++;

		salePrice = rs.getDouble(column);
		column++;

		purchasePrice = rs.getDouble(column);
		column++;

	suppliers = new SuppliersSet(false);
orders = new OrdersSet(false);

		idKey = itemId;
	}

	public Item(EntityManager em, ResultSet rs, String prefix) throws SQLException
	{
		this.em = em;

		itemId = rs.getInt(prefix + "_ItemId");

		name = rs.getString(prefix + "_Name");

		salePrice = rs.getDouble(prefix + "_SalePrice");

		purchasePrice = rs.getDouble(prefix + "_PurchasePrice");

	suppliers = new SuppliersSet(false);
orders = new OrdersSet(false);

		idKey = itemId;
	}

	Integer idKey;
	public Integer idKey()
	{
		return idKey;
	}
	
	Item comparisonCopy;
	public Item copyForComparison()
	{
		if (comparisonCopy != null) return comparisonCopy;
		Item copy = new Item();

		copy.itemId = itemId;

		copy.name = name;

		copy.salePrice = salePrice;

		copy.purchasePrice = purchasePrice;


		copy.suppliers = suppliers.comparisonClone();
		if (suppliers instanceof LazySet)
		{
			((LazySet<Supplier>)suppliers).setRealizeListener((LazySet<Supplier>)copy.suppliers);
		}


		copy.orders = orders.comparisonClone();
		if (orders instanceof LazySet)
		{
			((LazySet<LineOrder>)orders).setRealizeListener((LazySet<LineOrder>)copy.orders);
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
