package ch.epfl.labos.iu.orm.queryll2.symbolic;

import java.io.IOException;
import java.util.List;

import org.jinq.rebased.org.objectweb.asm.tree.analysis.AnalyzerException;
import org.junit.Assert;
import org.junit.Test;

import ch.epfl.labos.iu.orm.queryll2.path.MethodAnalysisResults;
import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisFactory;
import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisMethodChecker;
import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;
import ch.epfl.labos.iu.orm.queryll2.symbolic.BasicSymbolicInterpreter.OperationSideEffect;

public class BasicSymbolicInterpreterTest
{
   /** 
    * See if we can properly parse the appearance of 
    * StringConcatFactory if it appears in some string concatenation
    * code. 
    */
   @Test
   public void testStringConcatFactory() throws IOException, AnalyzerException
   {
      TransformationClassAnalyzer analyzer = new TransformationClassAnalyzer("ch.epfl.labos.iu.orm.queryll2.symbolic.BasicSymbolicInterpreterTest");
      PathAnalysisFactory pathAnalysisFactory = new PathAnalysisFactory(
            new PathAnalysisMethodChecker() {
               @Override
               public boolean isFluentChaining(MethodSignature m)
               {
                  return false;
               }

               @Override
               public boolean isPutFieldAllowed()
               {
                  return false;
               }

               @Override
               public OperationSideEffect isStaticMethodSafe(MethodSignature m)
               {
                  return OperationSideEffect.NONE;
               }

               @Override
               public OperationSideEffect isMethodSafe(MethodSignature m,
                     TypedValue base, List<TypedValue> args)
               {
                  return OperationSideEffect.NONE;
               }
            }); 
      MethodAnalysisResults analysis = analyzer.analyzeLambdaMethod("concat", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", pathAnalysisFactory);

      TypedValue returnValue = analysis.paths.get(0).getReturnValue();
      // Eclipse and older versions of the JDK will use StringBuilder.
      // But the StringConcatFactory will appear for newer JDK compilers
      Assert.assertTrue(
            (returnValue instanceof MethodCallValue && ((MethodCallValue)returnValue).name.equals("toString"))
            || (returnValue instanceof MethodCallValue.StringConcatFactoryValue));
   }
   
   public static String concat(String a, String b)
   {
      return a + b;
   }
}
