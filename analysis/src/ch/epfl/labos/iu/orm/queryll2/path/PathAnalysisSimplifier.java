package ch.epfl.labos.iu.orm.queryll2.path;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.epfl.labos.iu.orm.queryll2.symbolic.ConstantValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueRewriterWalker;

public class PathAnalysisSimplifier
{
   public static TypedValue simplify(TypedValue value, 
         Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> comparisonMethods, 
         Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> comparisonStaticMethods, 
         boolean isAllEqualsConverted)
   {
      TypedValue simplifiedBooleanReturnValue = value
            .visit(new TypedValueRewriterWalker<Object, RuntimeException>(new SymbExSimplifier<Object>(comparisonMethods, comparisonStaticMethods, isAllEqualsConverted)), null);
      simplifiedBooleanReturnValue = simplifiedBooleanReturnValue.visit(new SymbExBooleanRewriter(), false);
      return simplifiedBooleanReturnValue;
//      return value.visit(new TypedValueRewriterWalker<Object, RuntimeException>(new SymbExSimplifier<Object>(comparisonMethods)), null);
   }
//   public TypedValue getIsTrueReturnValue()
//   {
//      if (isTrueReturnValue == null)
//      {
//         isTrueReturnValue = new TypedValue.ComparisonValue(
//               TypedValue.ComparisonValue.ComparisonOp.ne, 
//               returnValue, 
//               new ConstantValue.IntegerConstant(0));
//      }
//      return isTrueReturnValue; 
//   }
   public static TypedValue simplifyBoolean(TypedValue value, Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> comparisonMethods, Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> comparisonStaticMethods, boolean isAllEqualsConverted)
   {
      TypedValue simplifiedBooleanReturnValue = value
            .visit(new TypedValueRewriterWalker<Object, RuntimeException>(new SymbExSimplifier<Object>(comparisonMethods, comparisonStaticMethods, isAllEqualsConverted)), null);
      simplifiedBooleanReturnValue = simplifiedBooleanReturnValue.visit(new SymbExBooleanRewriter(), true);
      return simplifiedBooleanReturnValue;
   }
//   public TypedValue getSimplifiedIsTrueReturnValue()
//   {
//      if (simplifiedIsTrueReturnValue == null)
//      {
//         simplifiedIsTrueReturnValue = getIsTrueReturnValue()
//               .visit(new TypedValueRewriterWalker<Object, RuntimeException>(new SymbExSimplifier<Object>()), null);
//      }
//      return simplifiedIsTrueReturnValue; 
//   }

   
// public List<TypedValue> getSimplifiedConditions()
// {
//    if (simplifiedConditions == null)
//    {
//       List<TypedValue> newConditions = new Vector<TypedValue>();
//       for (TypedValue.ComparisonValue cond: getConditions())
//          newConditions.add(cond.visit(new TypedValueRewriterWalker<Object, RuntimeException>(new SymbExSimplifier<Object>()), null));
//       simplifiedConditions = newConditions;
//    }
//    return simplifiedConditions; 
// }
   public static List<TypedValue> simplifyBooleans(List<TypedValue> conditions,
         Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> comparisonMethods,
         Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> comparisonStaticMethods,
         boolean isAllEqualsConverted)
   {
      List<TypedValue> newConditions = new ArrayList<TypedValue>();
      for (TypedValue cond: conditions)
      {
         TypedValue simpcond = cond.visit(new TypedValueRewriterWalker<Object, RuntimeException>(new SymbExSimplifier<Object>(comparisonMethods, comparisonStaticMethods, isAllEqualsConverted)), null);
         simpcond = simpcond.visit(new SymbExBooleanRewriter(), true);
         newConditions.add(simpcond);
      }
      return newConditions;
   }
   
   /**
    * Sometimes paths have weird conditions on them, so they can be pruned out.
    * (e.g. paths where the only condition is TRUE or FALSE)
    */
   public static void cleanAndSimplify(MethodAnalysisResults method,
         Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> comparisonMethods,
         Map<MethodSignature, TypedValue.ComparisonValue.ComparisonOp> comparisonStaticMethods,
         boolean isAllEqualsConverted)
   {
      List<PathAnalysis> pathsToDelete = new ArrayList<>();
      for (PathAnalysis path: method.paths)
      {
         List<TypedValue> simplifiedConditions = new ArrayList<>();
         for (TypedValue val: PathAnalysisSimplifier.simplifyBooleans(path.getConditions(), comparisonMethods, comparisonStaticMethods, isAllEqualsConverted))
         {
            if (val instanceof ConstantValue.BooleanConstant)
            {
               if (((ConstantValue.BooleanConstant)val).getConstant())
               {
                  // This part of the path condition is always TRUE, so it's
                  // redundant and can remove it as a path condition
               }
               else
               {
                  // One part of the path condition is FALSE, so the path condition
                  // can never hold. Prune out the whole path
                  pathsToDelete.add(path);
               }
            }
            simplifiedConditions.add(val);
         }
         path.conditions = simplifiedConditions;
      }
      method.paths.removeAll(pathsToDelete);
   }
}
