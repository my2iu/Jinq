package com.example.jinq.sample.jpa.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.persistence.*;


/**
 * The persistent class for the LINEORDERS database table.
 * 
 */
@Entity
@Table(name="LINEORDERS")
@IdClass(LineorderPK.class)
@NamedQuery(name="Lineorder.findAll", query="SELECT l FROM Lineorder l")
public class Lineorder implements Serializable {
   private static final long serialVersionUID = 1L;
   private BigDecimal total;
   private BigInteger transactionConfirmation;
   private int quantity;
   private Item item;
   private Sale sale;

   public Lineorder() {
   }


   public int getQuantity() {
      return this.quantity;
   }

   public void setQuantity(int quantity) {
      this.quantity = quantity;
   }

   public BigDecimal getTotal() {
      return this.total;
   }

   public void setTotal(BigDecimal total) {
      this.total = total;
   }

   public BigInteger getTransactionConfirmation() {
      return this.transactionConfirmation;
   }

   public void setTransactionConfirmation(BigInteger transactionConfirmation) {
      this.transactionConfirmation = transactionConfirmation;
   }

   //bi-directional many-to-one association to Item
   @ManyToOne @Id
   @JoinColumn(name="ITEMID")
   public Item getItem() {
      return this.item;
   }

   public void setItem(Item item) {
      this.item = item;
   }


   //bi-directional many-to-one association to Sale
   @ManyToOne @Id
   @JoinColumn(name="SALEID")
   public Sale getSale() {
      return this.sale;
   }

   public void setSale(Sale sale) {
      this.sale = sale;
   }

}