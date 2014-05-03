package org.jinq.jpa.transform;

import java.util.List;
import java.util.Vector;

import ch.epfl.labos.iu.orm.queryll2.path.SymbExBooleanRewriter;
import ch.epfl.labos.iu.orm.queryll2.path.SymbExSimplifier;
import ch.epfl.labos.iu.orm.queryll2.symbolic.ConstantValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueRewriterWalker;

public class PathAnalysis
{
   // TODO: Doing simplification of expressions at this stage using a rewriter
   // is ok, but it does mean that you can't simplify across nested queries
   // which may be necessary if a nested query has a parameter which refers
   // to something in the parent query, meaning it could be substituted in
   // and simplified

   
   public PathAnalysis(TypedValue returnValue, 
                       List<TypedValue.ComparisonValue> conditions)
   {
      this.returnValue = returnValue;
      this.conditions = conditions;
   }
   
   public TypedValue getReturnValue()
   {
      return returnValue;
   }
   public TypedValue getSimplifiedReturnValue()
   {
      if (simplifiedReturnValue == null)
      {
         simplifiedReturnValue = getReturnValue()
            .visit(new TypedValueRewriterWalker<Object, RuntimeException>(new SymbExSimplifier<Object>()), null);
      }
      return simplifiedReturnValue;
   }
   public TypedValue getIsTrueReturnValue()
   {
      if (isTrueReturnValue == null)
      {
         isTrueReturnValue = new TypedValue.ComparisonValue(
            TypedValue.ComparisonValue.ComparisonOp.ne, 
            returnValue, 
            new ConstantValue.IntegerConstant(0));
      }
      return isTrueReturnValue; 
   }
   public TypedValue getSimplifiedBooleanReturnValue()
   {
      if (simplifiedBooleanReturnValue == null)
      {
         simplifiedBooleanReturnValue = getReturnValue()
            .visit(new TypedValueRewriterWalker<Object, RuntimeException>(new SymbExSimplifier<Object>()), null);
         simplifiedBooleanReturnValue = simplifiedBooleanReturnValue.visit(new SymbExBooleanRewriter(), true); 
      }
      return simplifiedBooleanReturnValue;
   }
   public TypedValue getSimplifiedIsTrueReturnValue()
   {
      if (simplifiedIsTrueReturnValue == null)
      {
         simplifiedIsTrueReturnValue = getIsTrueReturnValue()
            .visit(new TypedValueRewriterWalker<Object, RuntimeException>(new SymbExSimplifier<Object>()), null);
      }
      return simplifiedIsTrueReturnValue; 
   }
   public List<TypedValue.ComparisonValue> getConditions()
   {
      return conditions;
   }
   public List<TypedValue> getSimplifiedConditions()
   {
      if (simplifiedConditions == null)
      {
         List<TypedValue> newConditions = new Vector<TypedValue>();
         for (TypedValue.ComparisonValue cond: getConditions())
            newConditions.add(cond.visit(new TypedValueRewriterWalker<Object, RuntimeException>(new SymbExSimplifier<Object>()), null));
         simplifiedConditions = newConditions;
      }
      return simplifiedConditions; 
   }
   public List<TypedValue> getSimplifiedBooleanConditions()
   {
      if (simplifiedBooleanConditions == null)
      {
         List<TypedValue> newConditions = new Vector<TypedValue>();
         for (TypedValue.ComparisonValue cond: getConditions())
         {
            TypedValue simpcond = cond.visit(new TypedValueRewriterWalker<Object, RuntimeException>(new SymbExSimplifier<Object>()), null);
            simpcond = simpcond.visit(new SymbExBooleanRewriter(), true);
            newConditions.add(simpcond);
         }
         simplifiedBooleanConditions = newConditions;
      }
      return simplifiedBooleanConditions; 
   }
   TypedValue simplifiedIsTrueReturnValue = null;
   TypedValue simplifiedReturnValue = null;
   TypedValue simplifiedBooleanReturnValue = null;
   TypedValue isTrueReturnValue = null;
   TypedValue returnValue;
   List<TypedValue.ComparisonValue> conditions;
   List<TypedValue> simplifiedConditions = null;
   List<TypedValue> simplifiedBooleanConditions = null;
   public void removeConditionIndex(int i)
   {
      conditions.remove(i);
      simplifiedConditions = null;
      simplifiedBooleanConditions = null;
   }
}
