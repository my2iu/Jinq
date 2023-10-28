package org.jinq.hibernate.test.entities;

import java.util.Date;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@MappedSuperclass
@Access(AccessType.PROPERTY)
public class SignatureSuperclass
{
   private byte[] signature;
   
   public byte[] getSignature() {
      return this.signature;
   }

   public void setSignature(byte[] signature) {
      this.signature = signature;
   }
   
   private Date signatureExpiry;
   
   @Temporal(TemporalType.TIMESTAMP)
   public Date getSignatureExpiry()
   {
      return signatureExpiry;
   }
   
   public void setSignatureExpiry(Date expiry)
   {
      this.signatureExpiry = expiry;
   }
}
