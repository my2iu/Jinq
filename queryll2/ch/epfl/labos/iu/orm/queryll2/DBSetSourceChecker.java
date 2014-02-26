package ch.epfl.labos.iu.orm.queryll2;

import java.util.Set;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitor;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class DBSetSourceChecker extends TypedValueVisitor<Set<TypedValue>, Boolean>
{
   ORMInformation entityInfo;
   public DBSetSourceChecker(ORMInformation entityInfo)
   {
      this.entityInfo = entityInfo;
   }
   
   @Override public Boolean virtualMethodCallValue(MethodCallValue.VirtualMethodCallValue val, Set<TypedValue> in) throws TypedValueVisitorException
   {
      MethodSignature sig = val.getSignature();
      if (entityInfo.dbSetMethods.contains(sig))
      {
         return true;   // The capture of the underlying dbset was probably already handled when the previous method was processed
      }
      else if (entityInfo.allEntityMethods.containsKey(sig))
      {
         return true;  // We probably don't need to check that the em is the same as that will happen later 
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
