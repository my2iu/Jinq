package org.jinq.jpa.transform;

import java.util.List;
import java.util.Set;

import scala.Function1;
import scala.Function2;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.BasicSymbolicInterpreter.OperationSideEffect;

public class ScalaMethodChecker extends MethodChecker
{
   ScalaMethodChecker(Set<Class<?>> safeMethodAnnotations,
         Set<MethodSignature> safeMethods,
         Set<MethodSignature> safeStaticMethods, 
         boolean isObjectEqualsSafe, boolean isCollectionContainsSafe)
   {
      super(safeMethodAnnotations, safeMethods, safeStaticMethods, isObjectEqualsSafe, isCollectionContainsSafe);
   }
   
   @Override
   public OperationSideEffect isMethodSafe(MethodSignature m, TypedValue base,
         List<TypedValue> args)
   {
      OperationSideEffect effect = super.isMethodSafe(m, base, args);
      if (effect != OperationSideEffect.UNSAFE)
         return effect;
      try
      {
         Class<?> c = Class.forName(m.getOwnerType().getClassName());
         if ("<init>".equals(m.name) &&
               (Function1.class.isAssignableFrom(c) || Function2.class.isAssignableFrom(c)) )
            return OperationSideEffect.SAFE;
      } catch (ClassNotFoundException e)
      {
         // We could not analyze the method, so we can't figure out if it's safe
         return OperationSideEffect.UNSAFE;
      }
      
      return effect; 
   }
   
   @Override
   public boolean isPutFieldAllowed()
   {
      return true;
   }

   @Override
   public boolean isFluentChaining(MethodSignature sig)
   {
      if (ScalaMetamodelUtil.STRINGBUILDER_APPEND.equals(sig)) return true;
      return super.isFluentChaining(sig);
   }

}
