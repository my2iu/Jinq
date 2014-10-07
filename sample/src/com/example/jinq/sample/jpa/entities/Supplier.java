package com.example.jinq.sample.jpa.entities;

import java.io.Serializable;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;


/**
 * The persistent class for the SUPPLIERS database table.
 * 
 */
@Entity
@Table(name="SUPPLIERS")
@NamedQuery(name="Supplier.findAll", query="SELECT s FROM Supplier s")
public class Supplier implements Serializable {
   private static final long serialVersionUID = 1L;
   private int supplierid;
   private String country;
   private String name;
   private List<Item> items = new ArrayList<>();
   private long revenue;
   private boolean hasFreeShipping;
   private byte[] signature;

   public Supplier() {
   }


   @Id
   @GeneratedValue(strategy=GenerationType.IDENTITY)
   @Column(updatable=false)
   public int getSupplierid() {
      return this.supplierid;
   }

   public void setSupplierid(int supplierid) {
      this.supplierid = supplierid;
   }

   public boolean getHasFreeShipping() {
      return this.hasFreeShipping;
   }

   public void setHasFreeShipping(boolean shipping) {
      this.hasFreeShipping = shipping;
   }

   public long getRevenue() {
      return this.revenue;
   }

   public void setRevenue(long revenue) {
      this.revenue = revenue;
   }

   public String getCountry() {
      return this.country;
   }

   public void setCountry(String country) {
      this.country = country;
   }


   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public byte[] getSignature() {
      return this.signature;
   }

   public void setSignature(byte[] signature) {
      this.signature = signature;
   }


   //bi-directional many-to-many association to Item
   @ManyToMany(mappedBy="suppliers")
   public List<Item> getItems() {
      return this.items;
   }

   public void setItems(List<Item> items) {
      this.items = items;
   }

}