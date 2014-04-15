package org.jinq.jpa.test.entities;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the LINEORDERS database table.
 * 
 */
@Entity
@Table(name="LINEORDERS")
@NamedQuery(name="Lineorder.findAll", query="SELECT l FROM Lineorder l")
public class Lineorder implements Serializable {
	private static final long serialVersionUID = 1L;
	private LineorderPK id;
	private int quantity;
	private Item item;
	private Sale sale;

	public Lineorder() {
	}


	@EmbeddedId
	public LineorderPK getId() {
		return this.id;
	}

	public void setId(LineorderPK id) {
		this.id = id;
	}


	public int getQuantity() {
		return this.quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}


	//bi-directional many-to-one association to Item
	@ManyToOne
	@JoinColumn(name="ITEMID",updatable=false,insertable=false)
	public Item getItem() {
		return this.item;
	}

	public void setItem(Item item) {
		this.item = item;
	}


	//bi-directional many-to-one association to Sale
	@ManyToOne
	@JoinColumn(name="SALEID",updatable=false,insertable=false)
	public Sale getSale() {
		return this.sale;
	}

	public void setSale(Sale sale) {
		this.sale = sale;
	}

}