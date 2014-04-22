package org.jinq.jpa.transform;

import java.util.List;
import java.util.Vector;

import ch.epfl.labos.iu.orm.queryll2.path.SymbExSimplifier;
import ch.epfl.labos.iu.orm.queryll2.symbolic.ConstantValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueRewriterWalker;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

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
   TypedValue simplifiedIsTrueReturnValue = null;
   TypedValue simplifiedReturnValue = null;
   TypedValue isTrueReturnValue = null;
   TypedValue returnValue;
   List<TypedValue.ComparisonValue> conditions;
   List<TypedValue> simplifiedConditions = null;
}
