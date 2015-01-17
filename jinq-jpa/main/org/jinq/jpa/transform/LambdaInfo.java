package org.jinq.jpa.transform;

import java.lang.reflect.Field;

import org.objectweb.asm.Type;

import com.user00.thunk.SerializedLambda;

/**
 * Holds surface information about a lambda such as the parameters to the 
 * lambda, the name of the method where the lambda comes from, etc.
 */
public class LambdaInfo
{
   protected Object Lambda;
   protected SerializedLambda serializedLambda;
   protected int numCapturedArgs;
   protected int numLambdaArgs;
   /**
    * JPAQueryComposer stores the lambdas that are chained together to create a query in a list.
    * The lambdaIndex refers to the index of this lambda in the list of lambdas used to create
    * the resulting query.
    */
   protected int lambdaIndex;

   public static LambdaInfo analyze(Object lambda, int lambdaIndex, boolean throwExceptionOnFailure)
   {
      SerializedLambda s;
      try {
         s = SerializedLambda.extractLambda(lambda);
         if (s == null) 
         {
            if (throwExceptionOnFailure) throw new IllegalArgumentException("Could not extract code from lambda. This error sometimes occurs because your lambda references objects that aren't Serializable.");
            return null;
         }
      } catch (Exception e)
      { 
         if (throwExceptionOnFailure) throw new IllegalArgumentException("Could not extract code from lambda. This error sometimes occurs because your lambda references objects that aren't Serializable.");
         return null;
      }
      return new LambdaInfo(lambda, s, lambdaIndex);
   }

   LambdaInfo(Object lambda, SerializedLambda serializedLambda, int lambdaIndex)
   {
      this.Lambda = lambda;
      this.serializedLambda = serializedLambda;
      this.lambdaIndex = lambdaIndex;
      this.numCapturedArgs = serializedLambda.capturedArgs.length;
      this.numLambdaArgs = Type.getArgumentTypes(serializedLambda.implMethodSignature).length;
   }
   
   protected LambdaInfo(int lambdaIndex, int numCapturedArgs, int numLambdaArgs)
   {
      this.Lambda = null;
      this.serializedLambda = null;
      this.lambdaIndex = lambdaIndex;
      this.numCapturedArgs = numCapturedArgs;
      this.numLambdaArgs = numLambdaArgs;
   }

   public int getNumCapturedArgs()
   {
      return numCapturedArgs;
   }

   public int getNumLambdaArgs()
   {
      return numLambdaArgs;
   }

   public boolean hasLambdaObject()
   {
      return serializedLambda != null;
   }

   public Object getCapturedArg(int argIndex)
   {
      return serializedLambda.capturedArgs[argIndex];
   }

   public Object getField(String fieldName)
   {
      try
      {
         Field field = Lambda.getClass().getDeclaredField(fieldName);
         field.setAccessible(true);
         return field.get(Lambda);
      } catch (SecurityException e) {
         throw new IllegalArgumentException("Cannot read field " + fieldName + " of lambda", e);
      } catch (Exception e)
      {
         try {
            return Lambda.getClass().getField(fieldName).get(Lambda);
         } catch (Exception e1)
         {
            throw new IllegalArgumentException("Cannot read field " + fieldName + " of lambda", e1);
         }
      }
   }
   
   public LambdaAnalysis fullyAnalyze(MetamodelUtil metamodel, ClassLoader alternateClassLoader, boolean isObjectEqualsSafe, boolean isCollectionContainsSafe, boolean throwExceptionOnFailure)
   {
      return LambdaAnalysis.fullyAnalyzeLambda(this, metamodel, alternateClassLoader, isObjectEqualsSafe, isCollectionContainsSafe, throwExceptionOnFailure);
   }

   /**
    * @return a string describing the source for the code of the lambda
    */
   public String getLambdaSourceString()
   {
      return serializedLambda.implClass + "#" + serializedLambda.implMethodName + serializedLambda.implMethodSignature; 
   }
}