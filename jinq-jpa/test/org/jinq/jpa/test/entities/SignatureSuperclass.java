package org.jinq.jpa.test.entities;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.MappedSuperclass;

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
}
