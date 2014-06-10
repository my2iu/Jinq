package org.jinq.jooq.transform;

import java.lang.reflect.Method;
import java.util.List;

import ch.epfl.labos.iu.orm.queryll2.path.Annotations;
import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisMethodChecker;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;

final class MethodChecker implements PathAnalysisMethodChecker
{
   MetamodelUtil metamodel;

   MethodChecker(MetamodelUtil metamodel)
   {
      this.metamodel = metamodel;
   }

   /* (non-Javadoc)
    * @see ch.epfl.labos.iu.orm.queryll2.PathAnalysisMethodChecker#isStaticMethodSafe(ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature)
    */
   @Override
   public boolean isStaticMethodSafe(MethodSignature m)
      { return metamodel.isSafeStaticMethod(m); }

   /* (non-Javadoc)
    * @see ch.epfl.labos.iu.orm.queryll2.PathAnalysisMethodChecker#isMethodSafe(ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature, ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue, java.util.List)
    */
   @Override
   public boolean isMethodSafe(MethodSignature m, TypedValue base, List<TypedValue> args)
      {
         if (metamodel.isSafeMethod(m))
         {
            return true;
         }
         else
         {
            // Use reflection to get info about the method (or would it be better
            // to do this through direct bytecode inspection?)
            try
            {
               Method reflectedMethod = Annotations.asmMethodSignatureToReflectionMethod(m);
               return Annotations.methodHasSomeAnnotations(reflectedMethod, metamodel.getSafeMethodAnnotations());
            } catch (ClassNotFoundException|NoSuchMethodException e)
            {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
            return false; 
            
         }
      }
}