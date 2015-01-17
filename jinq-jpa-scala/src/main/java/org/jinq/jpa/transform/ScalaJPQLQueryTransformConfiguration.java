package org.jinq.jpa.transform;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.tree.analysis.AnalyzerException;

import ch.epfl.labos.iu.orm.queryll2.path.MethodAnalysisResults;
import ch.epfl.labos.iu.orm.queryll2.path.MethodSideEffect;
import ch.epfl.labos.iu.orm.queryll2.path.MethodSideEffectCall;
import ch.epfl.labos.iu.orm.queryll2.path.MethodSideEffectFieldAssign;
import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysis;
import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisFactory;
import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue.ThisValue;

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
         SymbExArgumentHandler argumentHandler, boolean isExpectingStream)
   {
      if (!isExpectingStream) throw new IllegalArgumentException("Jinq for Scala uses implicit conversions and does not allow joins to non-streams");
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
   
   // TODO: This method redoes a lot of the same work as checkLambdaSideEffects, which is inefficient.
   @Override
   public Map<String, TypedValue> findLambdaAsClassConstructorParameters(MethodSignature sig, List<TypedValue> args) throws QueryTransformException
   {
      Map<String, TypedValue> indirectParamMapping = new HashMap<>();
      try {
         if (!sig.name.equals("<init>"))
            throw new IllegalArgumentException("Expecting a constructor method for a lambda");
         TransformationClassAnalyzer classAnalyzer = 
               new TransformationClassAnalyzer(sig.owner, alternateClassLoader);
         PathAnalysisFactory pathAnalysisFactory = new PathAnalysisFactory(
               metamodel.getMethodChecker(isObjectEqualsSafe, isCollectionContainsSafe));
         MethodAnalysisResults analysis = classAnalyzer.analyzeLambdaMethod(sig.name, sig.desc, pathAnalysisFactory);
         if (analysis == null) throw new QueryTransformException("Symbolic execution of constructor failed");
         if (analysis.paths.size() != 1) throw new QueryTransformException("Symbolic execution of constructor failed");
         for (MethodSideEffect effect: analysis.paths.get(0).getSideEffects())
         {
            // The constructor was already checked for safety, just extract the parameter information
            if (effect instanceof MethodSideEffectFieldAssign)
            {
               MethodSideEffectFieldAssign field = (MethodSideEffectFieldAssign)effect;
               if (!(field.base instanceof ThisValue))
                  throw new QueryTransformException("Lambda constructor is assigning to fields of other objects");
               if (!(field.value instanceof TypedValue.ArgValue))
                  throw new QueryTransformException("Jinq cannot analyze lambda constructors that transform their constructor parameters before storing them into fields");
               TypedValue.ArgValue arg = (TypedValue.ArgValue)field.value;
               indirectParamMapping.put(field.name, args.get(arg.getIndex()));
            }
         }
      } catch (IOException | AnalyzerException e)
      {
         throw new QueryTransformException("Could not analyze the side-effects of the lambda constructor", e); 
      }
      return indirectParamMapping;
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
                        metamodel.getMethodChecker(isObjectEqualsSafe, isCollectionContainsSafe));
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
