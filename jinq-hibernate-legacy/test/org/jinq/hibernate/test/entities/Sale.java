package org.jinq.hibernate.test.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.Type;

/**
 * The persistent class for the SALES database table.
 * 
 */
@Entity
@Table(name="SALES")
@NamedQuery(name="Sale.findAll", query="SELECT s FROM Sale s")
public class Sale implements Serializable {
   private static final long serialVersionUID = 1L;
   // This entity uses field-based access instead of property based access
   // in order to test how well Jinq handles this alternate JPA configuration.
   @Id
   @GeneratedValue(strategy=GenerationType.IDENTITY)
   @Column(updatable=false)
   private int saleid;
   //bi-directional many-to-one association to Lineorder
   @OneToMany(mappedBy="sale")
   private List<Lineorder> lineorders = new ArrayList<>();
   //bi-directional many-to-one association to Customer
   @ManyToOne
   @JoinColumn(name="CUSTOMERID")
   private Customer customer;
   private java.sql.Date sqlDate;
   private java.sql.Time sqlTime;
   private java.sql.Timestamp sqlTimestamp;
   @Temporal(TemporalType.TIMESTAMP)
   private Date date;
   @Temporal(TemporalType.DATE)
   private Calendar calendar;
   @Embedded
   private CreditCard creditCard;
   @Column(name="RUSH_ORDER")
   @Type(type="yes_no")
   private boolean rush;

   // Tests of new Java 8 java.time stuff for Hibernate 5.2+ only
   private Duration duration;
   private Instant instant;
   private LocalDateTime localDateTime;
   private LocalDate localDate;
   private LocalTime localTime;
   private OffsetDateTime offsetDateTime;
   private OffsetTime offsetTime;
   private ZonedDateTime zonedDateTime;

   
   public Sale() {
   }


   public int getSaleid() {
      return this.saleid;
   }

   public void setSaleid(int saleid) {
      this.saleid = saleid;
   }

   public Date getDate() {
      return this.date;
   }

   public void setDate(Date date) {
      this.date = date;
   }

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

   // Tests of new Java 8 java.time stuff for Hibernate 5.2+ only
   public Duration getDuration() { return duration; }
   public void setDuration(Duration duration) { this.duration = duration; }
   public Instant getInstant() { return instant; }
   public void setInstant(Instant instant) { this.instant = instant; }
   public LocalDateTime getLocalDateTime() { return localDateTime; }
   public void setLocalDateTime(LocalDateTime localDateTime) { this.localDateTime = localDateTime; }
   public LocalDate getLocalDate() { return localDate; }
   public void setLocalDate(LocalDate localDate) { this.localDate = localDate; }
   public LocalTime getLocalTime() { return localTime; }
   public void setLocalTime(LocalTime localTime) { this.localTime = localTime; }
   public OffsetDateTime getOffsetDateTime() { return offsetDateTime; }
   public void setOffsetDateTime(OffsetDateTime offsetDateTime) { this.offsetDateTime = offsetDateTime; }
   public OffsetTime getOffsetTime() { return offsetTime; }
   public void setOffsetTime(OffsetTime offsetTime) { this.offsetTime = offsetTime; }
   public ZonedDateTime getZonedDateTime() { return zonedDateTime; }
   public void setZonedDateTime(ZonedDateTime zonedDateTime) { this.zonedDateTime = zonedDateTime; }

   
   public CreditCard getCreditCard() 
   {
      return creditCard;
   }
   
   public void setCreditCard(CreditCard creditCard) {
      this.creditCard = creditCard;
   }

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


   public Customer getCustomer() {
      return this.customer;
   }

   public void setCustomer(Customer customer) {
      this.customer = customer;
   }

   public boolean isRush() 
   {
      return rush;
   }
   
   public void setRush(boolean isRush)
   {
      this.rush = isRush;
   }
}