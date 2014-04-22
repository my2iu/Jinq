package ch.epfl.labos.iu.orm.queryll2;

import java.util.Set;

import org.jinq.orm.annotations.EntitySupplier;

import ch.epfl.labos.iu.orm.queryll2.path.Annotations;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitor;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class JinqStreamSourceChecker extends TypedValueVisitor<Set<TypedValue>, Boolean, TypedValueVisitorException>
{
   ORMInformation entityInfo;
   public JinqStreamSourceChecker(ORMInformation entityInfo)
   {
      this.entityInfo = entityInfo;
   }

   @Override public Boolean defaultValue(TypedValue val, Set<TypedValue> in) throws TypedValueVisitorException
   {
      throw new TypedValueVisitorException("Unhandled symbolic execution operation: " + val);
   }

   @Override public Boolean virtualMethodCallValue(MethodCallValue.VirtualMethodCallValue val, Set<TypedValue> in) throws TypedValueVisitorException
   {
      MethodSignature sig = val.getSignature();
      if (entityInfo.jinqStreamMethods.contains(sig))
      {
         return true;   // The capture of the underlying jinq stream was probably already handled when the previous method was processed
      }
      else if (entityInfo.allEntityMethods.containsKey(sig))
      {
         return true;  // We probably don't need to check that the em is the same as that will happen later 
      }
      else
      {
         EntitySupplier entitySupplier = Annotations.methodFindAnnotation(sig, EntitySupplier.class);
         if (entitySupplier != null)
         {
            return true;
         }
      }
      return super.virtualMethodCallValue(val, in);
   }

   @Override public Boolean argValue(TypedValue.ArgValue val, Set<TypedValue> in) throws TypedValueVisitorException
   {
      // TODO: Not quite correct, but in practice this will catch everything
      // Assume that all DBSets passed in as arguments are ok
      return true;
   }

}
