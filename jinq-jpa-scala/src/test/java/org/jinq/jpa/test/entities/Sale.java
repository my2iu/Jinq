package org.jinq.jpa.test.entities;

import java.io.Serializable;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * The persistent class for the SALES database table.
 * 
 */
@Entity
@Table(name="SALES")
@NamedQuery(name="Sale.findAll", query="SELECT s FROM Sale s")
public class Sale implements Serializable {
   private static final long serialVersionUID = 1L;
   private int saleid;
   private List<Lineorder> lineorders = new ArrayList<>();
   private Customer customer;
   private java.sql.Date sqlDate;
   private java.sql.Time sqlTime;
   private java.sql.Timestamp sqlTimestamp;
   private Date date;
   private Calendar calendar;
   long creditCard;

   public Sale() {
   }


   @Id
   @GeneratedValue(strategy=GenerationType.IDENTITY)
   @Column(updatable=false)
   public int getSaleid() {
      return this.saleid;
   }

   public void setSaleid(int saleid) {
      this.saleid = saleid;
   }

   @Temporal(TemporalType.TIMESTAMP)
   public Date getDate() {
      return this.date;
   }

   public void setDate(Date date) {
      this.date = date;
   }

   @Temporal(TemporalType.DATE)
   public Calendar getCalendar() {
      return this.calendar;
   }

   public void setCalendar(Calendar cal) {
      this.calendar = cal;
   }

   public java.sql.Date getSqlDate() {
      return this.sqlDate;
   }

   public void setSqlDate(java.sql.Date date) {
      this.sqlDate = date;
   }

   public java.sql.Time getSqlTime() {
      return this.sqlTime;
   }

   public void setSqlTime(java.sql.Time time) {
      this.sqlTime = time;
   }

   public java.sql.Timestamp getSqlTimestamp() {
      return this.sqlTimestamp;
   }

   public void setSqlTimestamp(java.sql.Timestamp timestamp) {
      this.sqlTimestamp = timestamp;
   }
   
   public long getCreditCard() {
      return this.creditCard;
   }
   
   public void setCreditCard(long creditCard) {
      this.creditCard = creditCard;
   }


   //bi-directional many-to-one association to Lineorder
   @OneToMany(mappedBy="sale")
   public List<Lineorder> getLineorders() {
      return this.lineorders;
   }

   public void setLineorders(List<Lineorder> lineorders) {
      this.lineorders = lineorders;
   }

   public Lineorder addLineorder(Lineorder lineorder) {
      getLineorders().add(lineorder);
      lineorder.setSale(this);

      return lineorder;
   }

   public Lineorder removeLineorder(Lineorder lineorder) {
      getLineorders().remove(lineorder);
      lineorder.setSale(null);

      return lineorder;
   }


   //bi-directional many-to-one association to Customer
   @ManyToOne
   @JoinColumn(name="CUSTOMERID")
   public Customer getCustomer() {
      return this.customer;
   }

   public void setCustomer(Customer customer) {
      this.customer = customer;
   }

}