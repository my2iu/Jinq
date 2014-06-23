package ch.epfl.labos.iu.orm.queryll2.path;

import org.objectweb.asm.Type;

import ch.epfl.labos.iu.orm.queryll2.symbolic.ConstantValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitor;

public class SymbExSimplifier<I> extends TypedValueVisitor<I, TypedValue, RuntimeException>
{
   public TypedValue defaultValue(TypedValue val, I in) 
   {
      return val;
   }
   public TypedValue comparisonOpValue(TypedValue.ComparisonValue val, I in) 
   {
      // Check for comparison of two integer constants
      if (val.left instanceof ConstantValue.IntegerConstant
            && val.right instanceof ConstantValue.IntegerConstant)
      {
         ConstantValue.IntegerConstant left = (ConstantValue.IntegerConstant)val.left;
         ConstantValue.IntegerConstant right = (ConstantValue.IntegerConstant)val.right;
         switch(val.compOp)
         {
            case eq: return new ConstantValue.IntegerConstant(left.val == right.val ? 1: 0); 
            case ne: return new ConstantValue.IntegerConstant(left.val != right.val ? 1: 0);
            default: break;
         }
      }
      // Check for a comparison that is treated as an integer and used in a further comparison
      // (happens with methods that return true/false like String.equals())
      if (val.left instanceof ConstantValue.IntegerConstant
            && ((ConstantValue.IntegerConstant)val.left).val == 0
            && (val.compOp == TypedValue.ComparisonValue.ComparisonOp.eq 
                  || val.compOp == TypedValue.ComparisonValue.ComparisonOp.ne)
            && val.right instanceof TypedValue.ComparisonValue)
      {
         if (val.compOp != TypedValue.ComparisonValue.ComparisonOp.eq)
            return val.right;
         else
            return ((TypedValue.ComparisonValue)val.right).inverseValue();
      }
      if (val.right instanceof ConstantValue.IntegerConstant
            && ((ConstantValue.IntegerConstant)val.right).val == 0
            && (val.compOp == TypedValue.ComparisonValue.ComparisonOp.eq 
                  || val.compOp == TypedValue.ComparisonValue.ComparisonOp.ne)
            && val.left instanceof TypedValue.ComparisonValue)
      {
         if (val.compOp != TypedValue.ComparisonValue.ComparisonOp.eq)
            return val.left;
         else
            return ((TypedValue.ComparisonValue)val.left).inverseValue();
      }
      // Need to handle things like Util.SQLStringLike() differently,
      // so we check for things that return booleans and which are treated
      // like integers in a comparison
      if (val.right instanceof ConstantValue.IntegerConstant
            && ((ConstantValue.IntegerConstant)val.right).val == 0
            && (val.compOp == TypedValue.ComparisonValue.ComparisonOp.eq 
                  || val.compOp == TypedValue.ComparisonValue.ComparisonOp.ne)
            && val.left.getType() == Type.BOOLEAN_TYPE)
      {
         if (val.compOp != TypedValue.ComparisonValue.ComparisonOp.eq)
            return val.left;
         else
            return new TypedValue.NotValue(val.left);
      }
      if (val.left instanceof ConstantValue.IntegerConstant
            && ((ConstantValue.IntegerConstant)val.left).val == 0
            && (val.compOp == TypedValue.ComparisonValue.ComparisonOp.eq 
                  || val.compOp == TypedValue.ComparisonValue.ComparisonOp.ne)
            && val.right.getType() == Type.BOOLEAN_TYPE)
      {
         if (val.compOp != TypedValue.ComparisonValue.ComparisonOp.eq)
            return val.right;
         else
            return new TypedValue.NotValue(val.right);
      }
      
      return binaryOpValue(val, in);
   }

   public TypedValue virtualMethodCallValue(MethodCallValue.VirtualMethodCallValue val, I in) 
   {
      if (val.getSignature().equals(TransformationClassAnalyzer.stringEquals))
      {
         // TODO: This changes the semantics of things a little bit
         return new TypedValue.ComparisonValue(TypedValue.ComparisonValue.ComparisonOp.eq, val.base, val.args.get(0));
      }
      return methodCallValue(val, in);
   }

}
