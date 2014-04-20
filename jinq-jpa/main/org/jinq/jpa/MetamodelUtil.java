package org.jinq.jpa;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;

/**
 * Provides helper methods for extracting useful information from
 * a JPA metamodel.
 */
public class MetamodelUtil
{
   final Metamodel metamodel;

   public final Set<Class<?>> safeMethodAnnotations;
   public final Set<MethodSignature> safeMethods;
   public final Set<MethodSignature> safeStaticMethods;

   public MetamodelUtil(Metamodel metamodel)
   {
      this.metamodel = metamodel;
      safeMethodAnnotations = new HashSet<Class<?>>();
      safeMethodAnnotations.addAll(TransformationClassAnalyzer.SafeMethodAnnotations);
      safeMethods = new HashSet<MethodSignature>();
      safeMethods.addAll(TransformationClassAnalyzer.KnownSafeMethods);
      safeStaticMethods = new HashSet<MethodSignature>();
      safeStaticMethods.addAll(TransformationClassAnalyzer.KnownSafeStaticMethods);
      
      findMetamodelGetters();
   }
   
   private void findMetamodelGetters()
   {
      for (EntityType<?> entity: metamodel.getEntities())
      {
         for (SingularAttribute<?,?> singularAttrib: entity.getDeclaredSingularAttributes())
         {
            MethodSignature methodSig = new MethodSignature(
                  org.objectweb.asm.Type.getInternalName(singularAttrib.getJavaMember().getDeclaringClass()),
                  singularAttrib.getJavaMember().getName(),
                  org.objectweb.asm.Type.getMethodDescriptor(org.objectweb.asm.Type.getType(singularAttrib.getJavaType())));
            safeMethods.add(methodSig);
         }
      }
   }
   
   public <U> String entityNameFromClass(Class<U> entity)
   {
      EntityType<U> entityType = metamodel.entity(entity);
      if (entityType == null) return null;
      return entityType.getName();
   }
}
