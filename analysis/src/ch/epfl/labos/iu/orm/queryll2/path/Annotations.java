package ch.epfl.labos.iu.orm.queryll2.path;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;

import org.objectweb.asm.Type;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;

public class Annotations
{
   public static Class<?> asmTypeToClass(Type t) throws ClassNotFoundException
   {
      String name = t.getClassName();
      switch (name) 
      {
      case "char": return Character.TYPE;
      case "double": return Double.TYPE;
      case "float": return Float.TYPE;
      case "int": return Integer.TYPE;
      case "long": return Long.TYPE;
      case "short": return Short.TYPE;
      case "boolean": return Boolean.TYPE;
      }
      return Class.forName(name);
   }
   
   public static Method asmMethodSignatureToReflectionMethod(MethodSignature m) 
         throws ClassNotFoundException, NoSuchMethodException
   {
      Class<?> reflectedClass = Class.forName(m.getOwnerType().getClassName());
      Type []argTypes = Type.getMethodType(m.desc).getArgumentTypes();
      Class<?> []argClasses = new Class[argTypes.length];
      for (int n = 0; n < argTypes.length; n++)
         argClasses[n] = asmTypeToClass(argTypes[n]);
      return reflectedClass.getMethod(m.name, argClasses);
   }

   public static boolean methodHasSomeAnnotations(
         Method method, 
         final Collection<Class<?>> annotations)
   {
      for (Annotation a: method.getAnnotations())
      {
         for (Class<?> annotationClass: annotations)
            if (annotationClass.isInstance(a))
               return true;
      }
      return false;
   }
   
   public static <T extends Annotation> T methodFindAnnotation(MethodSignature sig, Class<T> annotation)
   {
      try
      {
         Method m = asmMethodSignatureToReflectionMethod(sig);
         return m.getAnnotation(annotation);
      } catch (ClassNotFoundException | NoSuchMethodException e)
      {
         e.printStackTrace();
         return null;
      }
      
   }
}
