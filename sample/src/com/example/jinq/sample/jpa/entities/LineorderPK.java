package com.example.jinq.sample.jpa.entities;

import java.io.Serializable;

import javax.persistence.*;

/**
 * The primary key class for the LINEORDERS database table.
 * 
 */
@Embeddable
public class LineorderPK implements Serializable {
   //default serial version id, required for serializable classes.
   private static final long serialVersionUID = 1L;
   private int sale;
   private int item;

   public LineorderPK() {
   }

   public int getSale() {
      return this.sale;
   }
   public void setSale(int sale) {
      this.sale = sale;
   }

   public int getItem() {
      return this.item;
   }
   public void setItem(int item) {
      this.item = item;
   }

   public boolean equals(Object other) {
      if (this == other) {
         return true;
      }
      if (!(other instanceof LineorderPK)) {
         return false;
      }
      LineorderPK castOther = (LineorderPK)other;
      return 
            (this.sale == castOther.sale)
            && (this.item == castOther.item);
   }

   public int hashCode() {
      final int prime = 31;
      int hash = 17;
      hash = hash * prime + this.sale;
      hash = hash * prime + this.item;

      return hash;
   }
}