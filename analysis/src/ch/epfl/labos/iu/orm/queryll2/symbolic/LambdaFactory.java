package ch.epfl.labos.iu.orm.queryll2.symbolic;

import java.util.List;

import org.jinq.rebased.org.objectweb.asm.Handle;
import org.jinq.rebased.org.objectweb.asm.Opcodes;
import org.jinq.rebased.org.objectweb.asm.Type;

public class LambdaFactory extends TypedValue
{
   Handle lambdaMethod;
   List<TypedValue> capturedArgs;
   
   // TODO: Handle parameters passed in to the lambda 
   public LambdaFactory(Type functionalInterface, Handle lambdaMethod, List<TypedValue> capturedArgs)
   {
      super(functionalInterface);
      this.lambdaMethod = lambdaMethod;
      this.capturedArgs = capturedArgs;
   }
   
   public String toString()
   {
      return "LambdaFactory(" + lambdaMethod.getOwner() + "." 
            + lambdaMethod.getName() + lambdaMethod.getDesc() + ")";
   }

   public Handle getLambdaMethod()
   {
      return lambdaMethod;
   }
   
   public boolean isInvokeStatic()
   {
      return lambdaMethod.getTag() == Opcodes.H_INVOKESTATIC;
   }

   public boolean isInvokeVirtual()
   {
      return lambdaMethod.getTag() == Opcodes.H_INVOKEVIRTUAL;
   }

   public List<TypedValue> getCapturedArgs()
   {
      return capturedArgs;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result
            + ((capturedArgs == null) ? 0 : capturedArgs.hashCode());
      result = prime * result
            + ((lambdaMethod == null) ? 0 : lambdaMethod.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (getClass() != obj.getClass())
         return false;
      LambdaFactory other = (LambdaFactory) obj;
      if (capturedArgs == null)
      {
         if (other.capturedArgs != null)
            return false;
      } else if (!capturedArgs.equals(other.capturedArgs))
         return false;
      if (lambdaMethod == null)
      {
         if (other.lambdaMethod != null)
            return false;
      } else if (!lambdaMethod.equals(other.lambdaMethod))
         return false;
      return true;
   }

}
