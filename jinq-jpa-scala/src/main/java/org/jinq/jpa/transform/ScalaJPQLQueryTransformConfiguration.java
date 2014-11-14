package org.jinq.jpa.transform;

import java.io.IOException;

import org.objectweb.asm.tree.analysis.AnalyzerException;

import ch.epfl.labos.iu.orm.queryll2.path.MethodAnalysisResults;
import ch.epfl.labos.iu.orm.queryll2.path.MethodSideEffect;
import ch.epfl.labos.iu.orm.queryll2.path.MethodSideEffectCall;
import ch.epfl.labos.iu.orm.queryll2.path.MethodSideEffectFieldAssign;
import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysis;
import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisFactory;
import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;

public class ScalaJPQLQueryTransformConfiguration extends
      JPQLQueryTransformConfiguration
{
   @Override
   public SymbExToColumns newSymbExToColumns(
         SymbExArgumentHandler argumentHandler)
   {
      return new ScalaSymbExToColumns(this, argumentHandler);
   }

   @Override
   public SymbExToSubQuery newSymbExToSubQuery(
         SymbExArgumentHandler argumentHandler)
   {
      return new ScalaSymbExToSubQuery(this, argumentHandler);
   }

   /**
    * Analyzes the side-effects of a constructor for a lambda.
    * @param analysis
    */
   private final static MethodSignature abstractFunction1Constructor = new MethodSignature("scala/runtime/AbstractFunction1", "<init>", "()V");
   private void analyzeConstructor(MethodAnalysisResults analysis) throws QueryTransformException
   {
      if (analysis == null) throw new QueryTransformException("Symbolic execution of constructor failed");
      if (analysis.paths.size() != 1) throw new QueryTransformException("Symbolic execution of constructor failed");
      for (MethodSideEffect effect: analysis.paths.get(0).getSideEffects())
      {
         if (effect instanceof MethodSideEffectCall)
         {
            MethodSideEffectCall call = (MethodSideEffectCall) effect;
            if (call.m.equals(abstractFunction1Constructor)) continue;
         }
         else if (effect instanceof MethodSideEffectFieldAssign)
         {
            // Assigning to local fields is safe as long as the constructed object doesn't escape the lambda.
            continue;
         }
         else
            throw new QueryTransformException("Lambda calls a constructor with unknown side-effects");
      }
   }
   
   @Override
   public void checkLambdaSideEffects(LambdaAnalysis lambda) throws QueryTransformException
   {
      for (PathAnalysis path: lambda.symbolicAnalysis.paths)
      {
         for (MethodSideEffect effect: path.getSideEffects())
         {
            if (effect instanceof MethodSideEffectCall)
            {
               MethodSideEffectCall call = (MethodSideEffectCall)effect;
               if (!call.m.name.equals("<init>"))
                  throw new QueryTransformException("Lambda has a side-effect that can't be emulated with a database query");
               try {
                  TransformationClassAnalyzer classAnalyzer = 
                        new TransformationClassAnalyzer(call.m.owner, alternateClassLoader);
                  PathAnalysisFactory pathAnalysisFactory = new PathAnalysisFactory(
                        metamodel.getMethodChecker(isObjectEqualsSafe));
                  MethodAnalysisResults analysis = classAnalyzer.analyzeLambdaMethod(call.m.name, call.m.desc, pathAnalysisFactory);
                  analyzeConstructor(analysis);
                  continue;
               } catch (AnalyzerException | IOException e) {
                  throw new QueryTransformException("Could not analyze the side-effects of the lambda to check for safety", e); 
               }
            }
            throw new QueryTransformException("Lambda has a side-effect that can't be emulated with a database query"); 
         }
      }
   }
}
