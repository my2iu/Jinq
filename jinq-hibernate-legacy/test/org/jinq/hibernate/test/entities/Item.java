package org.jinq.hibernate.test.entities;

import java.io.Serializable;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;


/**
 * The persistent class for the ITEMS database table.
 * 
 */
@Entity
@Table(name="ITEMS")
@NamedQuery(name="Item.findAll", query="SELECT i FROM Item i")
public class Item implements Serializable {
	private static final long serialVersionUID = 1L;
	private int itemid;
	private String name;
	private double purchaseprice;
	private double saleprice;
	private List<Supplier> suppliers = new ArrayList<>();
	private List<Lineorder> lineorders = new ArrayList<>();
	private ItemType type;
	
	public Item() {
	}


	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(updatable=false)
	public int getItemid() {
		return this.itemid;
	}

	public void setItemid(int itemid) {
		this.itemid = itemid;
	}


	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ItemType getType() {
	   return this.type;
	}

	public void setType(ItemType type) {
	   this.type = type;
	}


	public double getPurchaseprice() {
		return this.purchaseprice;
	}

	public void setPurchaseprice(double purchaseprice) {
		this.purchaseprice = purchaseprice;
	}


	public double getSaleprice() {
		return this.saleprice;
	}

	public void setSaleprice(double saleprice) {
		this.saleprice = saleprice;
	}


	//bi-directional many-to-many association to Supplier
	@ManyToMany
	@JoinTable(
		name="ITEMSUPPLIERS",
		joinColumns={ @JoinColumn(name="ITEMID") },
		inverseJoinColumns={ @JoinColumn(name="SUPPLIERID") }
		)
	public List<Supplier> getSuppliers() {
		return this.suppliers;
	}

	public void setSuppliers(List<Supplier> suppliers) {
		this.suppliers = suppliers;
	}


	//bi-directional many-to-one association to Lineorder
	@OneToMany(mappedBy="item")
	public List<Lineorder> getLineorders() {
		return this.lineorders;
	}

	public void setLineorders(List<Lineorder> lineorders) {
		this.lineorders = lineorders;
	}

	public Lineorder addLineorder(Lineorder lineorder) {
		getLineorders().add(lineorder);
		lineorder.setItem(this);

		return lineorder;
	}

	public Lineorder removeLineorder(Lineorder lineorder) {
		getLineorders().remove(lineorder);
		lineorder.setItem(null);

		return lineorder;
	}
	
        public boolean equals(Object obj) {
           return obj == this;
        }
}