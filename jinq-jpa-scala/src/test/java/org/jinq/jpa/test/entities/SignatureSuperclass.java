package org.jinq.jpa.test.entities;

import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

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
