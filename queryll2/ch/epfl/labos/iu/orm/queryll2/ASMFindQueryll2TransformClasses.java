package ch.epfl.labos.iu.orm.queryll2;

import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Uses ASM to analyze classes.
 *
 */

public class ASMFindQueryll2TransformClasses
{
   static class IsTransformationVisitor extends ClassVisitor
   {
      public IsTransformationVisitor(int api)
    {
      super(api);
      // TODO Auto-generated constructor stub
    }
      boolean isTransformation = false;
      String name = null;
      public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) 
      {
         this.name = name;
         if (TransformationClassAnalyzer.TransformationClassMethods.containsKey(superName))
            isTransformation = true;
         for (String i: interfaces)
            if (TransformationClassAnalyzer.TransformationClassMethods.containsKey(i))
               isTransformation = true;
      }            
      public void visitSource(String arg0, String arg1) {}
      public void visitOuterClass(String arg0, String arg1, String arg2) {}
      public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {return null;} 
      public void visitAttribute(Attribute arg0) {}
      public void visitInnerClass(String arg0, String arg1, String arg2, int arg3) {}
      public FieldVisitor visitField(int arg0, String arg1, String arg2, String arg3, Object arg4) {return null;}
      public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {return null;} 
      public void visitEnd() {}
   };
   // Returns the name of the class if it is a transformation, or null otherwise
   static public String isTransformation(InputStream in)
   {
      IsTransformationVisitor cv = new IsTransformationVisitor(Opcodes.ASM5);
      try
      {
         ClassReader cr = new ClassReader(in);
         cr.accept(cv, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);
      } catch (IOException e)
      {
         e.printStackTrace();
      }
      return cv.isTransformation ? cv.name : null;
   }
}
