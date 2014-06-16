
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

public class LineOrder implements Cloneable
{
	EntityManager em;

	public LineOrder()
	{



		isItemRealized = true;


		isSaleRealized = true;

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
  
		
	
	private int quantity;
	public int getQuantity()
	{
		return quantity;
	}
	public void setQuantity(int _val)
	{
		if (em != null)
			em.dirtyInstance(this);
		quantity = _val;
	}

	

	boolean isItemRealized;
	String _linkcol_item_ItemId;
	Item item;
	public Item getItem()
	{
		if (!isItemRealized)
		{
			try {
				PreparedStatement stmt = em.db.con.prepareStatement("SELECT "
					+ " 1, C.ItemId as C_ItemId, C.Name as C_Name, C.SalePrice as C_SalePrice, C.PurchasePrice as C_PurchasePrice, C.ItemId as LINK_C_ItemId, C.ItemId as LINK_C_ItemId"
					+ " FROM Items as C"
					+ " WHERE 1=1"
	
					+ " AND C.ItemId = " + _linkcol_item_ItemId
					);
				ResultSet rs = stmt.executeQuery();
				if (rs.next())
					item = em.createItem(rs, "C");
				rs.close();
				stmt.close();
				isItemRealized = true;
			} catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
		return item;
	}
	public void setItem(Item _val)
	{
		if (em != null)
			em.dirtyInstance(this);
		if (getItem() != null)
			item.getOrders().remove(this);
		item = _val;
		_val.getOrders().add(this);
	}


	boolean isSaleRealized;
	String _linkcol_sale_SaleId;
	Sale sale;
	public Sale getSale()
	{
		if (!isSaleRealized)
		{
			try {
				PreparedStatement stmt = em.db.con.prepareStatement("SELECT "
					+ " 1, C.SaleId as C_SaleId, C.Date as C_Date, C.SaleId as LINK_C_SaleId, C.CustomerId as LINK_C_CustomerId"
					+ " FROM Sales as C"
					+ " WHERE 1=1"
	
					+ " AND C.SaleId = " + _linkcol_sale_SaleId
					);
				ResultSet rs = stmt.executeQuery();
				if (rs.next())
					sale = em.createSale(rs, "C");
				rs.close();
				stmt.close();
				isSaleRealized = true;
			} catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
		return sale;
	}
	public void setSale(Sale _val)
	{
		if (em != null)
			em.dirtyInstance(this);
		sale = _val;
		isSaleRealized = true;
	}


	public void persist(EntityManager em)
	{
		this.em = em;
		idKey = new Pair<Integer, Integer>(saleId, itemId);
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


		setItem(null);


		if (getSale() != null)
			getSale().getSaleLine().remove(this);

		if (em != null)
			em.dispose(this);
		em = null;
	}
	
	public LineOrder(EntityManager em, ResultSet rs, int column) throws SQLException
	{
		this.em = em;

		
		saleId = rs.getInt(column);
		column++;
  
		itemId = rs.getInt(column);
		column++;
  
		quantity = rs.getInt(column);
		column++;

	

		isItemRealized = false;
		_linkcol_item_ItemId = rs.getString(column);
		column++;
		

		isSaleRealized = false;
		_linkcol_sale_SaleId = rs.getString(column);
		column++;
		
		idKey = new Pair<Integer, Integer>(saleId, itemId);
	}

	public LineOrder(EntityManager em, ResultSet rs, String prefix) throws SQLException
	{
		this.em = em;

		
		saleId = rs.getInt(prefix + "_SaleId");
  
		itemId = rs.getInt(prefix + "_ItemId");
  
		quantity = rs.getInt(prefix + "_Quantity");

	

		isItemRealized = false;
		_linkcol_item_ItemId = rs.getString("LINK_" + prefix + "_ItemId");
		

		isSaleRealized = false;
		_linkcol_sale_SaleId = rs.getString("LINK_" + prefix + "_SaleId");
		
		idKey = new Pair<Integer, Integer>(saleId, itemId);
	}

	Pair<Integer, Integer> idKey;
	public Pair<Integer, Integer> idKey()
	{
		return idKey;
	}
	
	LineOrder comparisonCopy;
	public LineOrder copyForComparison()
	{
		if (comparisonCopy != null) return comparisonCopy;
		LineOrder copy = new LineOrder();

		copy.saleId = saleId;

		copy.itemId = itemId;

		copy.quantity = quantity;

		
		
		
	
		
		
		
	
		comparisonCopy = copy;
		return copy;
	}
	
	public Object clone() throws CloneNotSupportedException
	{	
		// TODO: This is bogus (doesn't handle sets correctly)
		return super.clone();
	}
}
