
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

public class ItemSupplier implements Cloneable
{
	EntityManager em;

	public ItemSupplier()
	{


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

	

	public void persist(EntityManager em)
	{
		this.em = em;
		idKey = new Pair<Integer, Integer>(itemId, supplierId);
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

		if (em != null)
			em.dispose(this);
		em = null;
	}
	
	public ItemSupplier(EntityManager em, ResultSet rs, int column) throws SQLException
	{
		this.em = em;

		itemId = rs.getInt(column);
		column++;

		supplierId = rs.getInt(column);
		column++;

	
		idKey = new Pair<Integer, Integer>(itemId, supplierId);
	}

	public ItemSupplier(EntityManager em, ResultSet rs, String prefix) throws SQLException
	{
		this.em = em;

		itemId = rs.getInt(prefix + "_ItemId");

		supplierId = rs.getInt(prefix + "_SupplierId");

	
		idKey = new Pair<Integer, Integer>(itemId, supplierId);
	}

	Pair<Integer, Integer> idKey;
	public Pair<Integer, Integer> idKey()
	{
		return idKey;
	}
	
	ItemSupplier comparisonCopy;
	public ItemSupplier copyForComparison()
	{
		if (comparisonCopy != null) return comparisonCopy;
		ItemSupplier copy = new ItemSupplier();

		copy.itemId = itemId;

		copy.supplierId = supplierId;

		comparisonCopy = copy;
		return copy;
	}
	
	public Object clone() throws CloneNotSupportedException
	{	
		// TODO: This is bogus (doesn't handle sets correctly)
		return super.clone();
	}
}
