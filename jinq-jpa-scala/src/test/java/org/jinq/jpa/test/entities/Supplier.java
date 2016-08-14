package org.jinq.jpa.test.entities;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the SUPPLIERS database table.
 * 
 * This entity uses a MappedSuperclass to store some of its fields.
 */
@Entity
@Table(name="SUPPLIERS")
@NamedQuery(name="Supplier.findAll", query="SELECT s FROM Supplier s")
public class Supplier extends SignatureSuperclass implements Serializable {
   private static final long serialVersionUID = 1L;
   private int supplierid;
   private String country;
   private String name;
   private List<Item> items;
   private long revenue;
   private boolean hasFreeShipping;
   private boolean preferredSupplier;
   private float paymentDiscount;

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

   public boolean isPreferredSupplier() {
      return this.preferredSupplier;
   }

   public void setPreferredSupplier(boolean isPreferred) {
      this.preferredSupplier = isPreferred;
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

   public float getPaymentDiscount() {
      return this.paymentDiscount;
   }

   public void setPaymentDiscount(float paymentDiscount) {
      this.paymentDiscount = paymentDiscount;
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