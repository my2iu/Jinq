package ch.epfl.labos.iu.orm.queryll2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import ch.epfl.labos.iu.orm.queryll2.symbolic.ConstantValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
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
                       List<TypedValue.ComparisonValue> conditions, 
                       List<MethodSignature> transformConstructorsCalled,
                       Set<TypedValue> unresolvedDBSets,
                       Set<TypedValue> unresolvedJinqStreams)
   {
      this.returnValue = returnValue;
      this.conditions = conditions;
      this.transformConstructorsCalled = new HashSet<MethodSignature>();
      this.transformConstructorsCalled.addAll(transformConstructorsCalled);
      this.unresolvedDBSets = new HashSet<TypedValue>();
      this.unresolvedDBSets.addAll(unresolvedDBSets);
      this.unresolvedJinqStreams = new HashSet<TypedValue>();
      this.unresolvedJinqStreams.addAll(unresolvedJinqStreams);
   }
   
   public TypedValue getReturnValue()
   {
      return returnValue;
   }
   public TypedValue getSimplifiedReturnValue()
   {
      if (simplifiedReturnValue == null)
      {
         try {
            simplifiedReturnValue = getReturnValue()
               .visit(new TypedValueRewriterWalker<Object>(new SymbExSimplifier<Object>()), null);
         } catch (TypedValueVisitorException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
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
         try {
            simplifiedIsTrueReturnValue = getIsTrueReturnValue()
               .visit(new TypedValueRewriterWalker<Object>(new SymbExSimplifier<Object>()), null);
         } catch (TypedValueVisitorException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
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
         try {
            List<TypedValue> newConditions = new Vector<TypedValue>();
            for (TypedValue.ComparisonValue cond: getConditions())
               newConditions.add(cond.visit(new TypedValueRewriterWalker<Object>(new SymbExSimplifier<Object>()), null));
            simplifiedConditions = newConditions;
         } catch (TypedValueVisitorException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
      return simplifiedConditions; 
   }
   public Set<MethodSignature> getTransformConstructorsCalled()
   {
      return transformConstructorsCalled;
   }
   public Set<TypedValue> getUnresolvedDBSets()
   {
      return unresolvedDBSets;
   }
   public Set<TypedValue> getUnresolvedJinqStreams()
   {
      return unresolvedJinqStreams;
   }
   TypedValue simplifiedIsTrueReturnValue = null;
   TypedValue simplifiedReturnValue = null;
   TypedValue isTrueReturnValue = null;
   TypedValue returnValue;
   List<TypedValue.ComparisonValue> conditions;
   List<TypedValue> simplifiedConditions = null;
   Set<MethodSignature> transformConstructorsCalled;
   Set<TypedValue> unresolvedDBSets;
   Set<TypedValue> unresolvedJinqStreams;
}
