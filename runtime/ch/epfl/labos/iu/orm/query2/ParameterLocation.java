package ch.epfl.labos.iu.orm.query2;

import java.lang.reflect.Field;

import com.user00.thunk.SerializedLambda;

import ch.epfl.labos.iu.orm.query2.SQLSubstitution.FromReference;

public abstract class ParameterLocation
{
   public abstract Object getParameter(Object thisBase) throws QueryGenerationException;
   public abstract int getLambdaIndex();
   
   public static ParameterLocation createJava8LambdaArgAccess(int argIndex, int lambdaIndex)
   {
      return new LambdaCapturedArg(argIndex, lambdaIndex);
   }
   public static ParameterLocation createThisFieldAccess(String fieldName, int lambdaIndex)
   {
      ParameterLocation toReturn = new Field(new This(lambdaIndex), fieldName);
      return toReturn;
   }
   
   public static abstract class MethodGetter extends ParameterLocation
   {
      
   }
   public static class LambdaCapturedArg extends ParameterLocation
   {
      int index;
      int lambdaIndex;
      LambdaCapturedArg(int index, int lambdaIndex)
      {
         this.index = index;
         this.lambdaIndex = lambdaIndex;
      }
      public int hashCode()
      {
         return index << 16 ^ lambdaIndex;
      }
      public boolean equals(Object o)
      {
         if (!(o instanceof LambdaCapturedArg)) return false;
         LambdaCapturedArg other = (LambdaCapturedArg)o;
         return other.index == index && other.lambdaIndex == lambdaIndex;
      }
      public int getLambdaIndex()
      {
         return lambdaIndex;
      }
      public Object getParameter(Object thisBase) throws QueryGenerationException
      {
         try {
            SerializedLambda s = SerializedLambda.extractLambda(thisBase);
            return s.capturedArgs[index];
         } catch (Exception e) {
            throw new QueryGenerationException("Error reading a field parameter", e);
         } 
      }
   }
   public static class Field extends ParameterLocation
   {
      ParameterLocation base;
      String fieldName;
      Field(ParameterLocation base, String fieldName)
      {
         this.base = base;
         this.fieldName = fieldName;
      }
      public int hashCode()
      {
         return base.hashCode() ^ fieldName.hashCode();
      }
      public boolean equals(Object o)
      {
         if (!(o instanceof Field)) return false;
         Field other = (Field)o;
         return other.base.equals(base) && other.fieldName.equals(fieldName);
      }
      public int getLambdaIndex()
      {
         return base.getLambdaIndex();
      }
      public Object getParameter(Object thisBase) throws QueryGenerationException
      {
         try {
            Object obj = base.getParameter(thisBase);
            // TODO: Does this pick up inherited fields?
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object param = field.get(obj);
            return param;
         } catch (Exception e) {
            throw new QueryGenerationException("Error reading a field parameter", e);
         } 
      }

   }
   public static class This extends ParameterLocation
   {
      int lambdaIndex;
      public This(int lambdaIndex)
      {
         this.lambdaIndex = lambdaIndex;
      }
      public int hashCode()
      {
         return "this".hashCode();
      }
      public boolean equals(Object o)
      {
         if (!(o instanceof This)) return false;
         This other = (This)o;
         return other.lambdaIndex == lambdaIndex;
      }
      public int getLambdaIndex()
      {
         return lambdaIndex;
      }
      public Object getParameter(Object thisBase) throws QueryGenerationException
      {
         return thisBase;
      }
   }
}
