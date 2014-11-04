package org.jinq.jpa.transform;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.metamodel.Metamodel;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;

public class ScalaMetamodelUtil extends MetamodelUtil
{
   public final static MethodSignature INQUERYSTREAMSOURCE_STREAM = new MethodSignature("org/jinq/orm/stream/scala/InQueryStreamSource", "stream", "(Ljava/lang/Class;)Lorg/jinq/orm/stream/scala/JinqScalaStream;");

   private Set<MethodSignature> safeMethods;
   private Set<MethodSignature> oldSafeMethods = null;

   public ScalaMetamodelUtil(Metamodel metamodel)
   {
      super(metamodel);
   }

   private void calculateScalaSafeMethods(Set<MethodSignature> javaSafeMethods)
   {
      Set<MethodSignature> newSafeMethods = new HashSet<>();
      newSafeMethods.addAll(javaSafeMethods);
      newSafeMethods.add(INQUERYSTREAMSOURCE_STREAM);
      safeMethods = newSafeMethods;
      oldSafeMethods = javaSafeMethods;
   }

   @Override
   public Set<MethodSignature> getSafeMethods()
   {
      Set<MethodSignature> superSafeMethods = super.getSafeMethods();
      if (superSafeMethods == oldSafeMethods)
         return safeMethods;
      calculateScalaSafeMethods(superSafeMethods);
      return safeMethods;
   }
}
