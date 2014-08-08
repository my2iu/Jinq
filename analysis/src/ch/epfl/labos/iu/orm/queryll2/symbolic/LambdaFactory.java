package ch.epfl.labos.iu.orm.queryll2.symbolic;

import java.util.List;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

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
   
   public List<TypedValue> getCapturedArgs()
   {
      return capturedArgs;
   }

}
