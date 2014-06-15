package ch.epfl.labos.iu.orm.queryll2.symbolic;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

public class LambdaFactory extends TypedValue
{
   Handle lambdaMethod;
   
   // TODO: Handle parameters passed in to the lambda 
   public LambdaFactory(Type functionalInterface, Handle lambdaMethod)
   {
      super(functionalInterface);
      this.lambdaMethod = lambdaMethod;
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
}
