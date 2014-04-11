package ch.epfl.labos.iu.orm.queryll2.symbolic;

public abstract class TypedValueVisitor<I, O>
{
   public O defaultValue(TypedValue val, I in) throws TypedValueVisitorException
   {
      throw new TypedValueVisitorException("Unhandled symbolic execution operation: " + val);
   }

   public O unknownValue(TypedValue val, I in) throws TypedValueVisitorException
   {
      return defaultValue(val, in);
   }
   public O newValue(TypedValue.NewValue val, I in) throws TypedValueVisitorException
   {
      return defaultValue(val, in);
   }
   
   public O varValue(TypedValue val, I in) throws TypedValueVisitorException
   {
      return defaultValue(val, in);
   }

   public O thisValue(TypedValue.ThisValue val, I in) throws TypedValueVisitorException
   {
      return varValue(val, in);
   }
   public O argValue(TypedValue.ArgValue val, I in) throws TypedValueVisitorException
   {
      return varValue(val, in);
   }

   public O constantValue(ConstantValue val, I in) throws TypedValueVisitorException
   {
      return varValue(val, in);
   }
   public O byteConstantValue(ConstantValue.ByteConstant val, I in) throws TypedValueVisitorException
   {
      return constantValue(val, in);
   }
   public O shortConstantValue(ConstantValue.ShortConstant val, I in) throws TypedValueVisitorException
   {
      return constantValue(val, in);
   }
   public O integerConstantValue(ConstantValue.IntegerConstant val, I in) throws TypedValueVisitorException
   {
      return constantValue(val, in);
   }
   public O longConstantValue(ConstantValue.LongConstant val, I in) throws TypedValueVisitorException
   {
      return constantValue(val, in);
   }
   public O floatConstantValue(ConstantValue.FloatConstant val, I in) throws TypedValueVisitorException
   {
      return constantValue(val, in);
   }
   public O doubleConstantValue(ConstantValue.DoubleConstant val, I in) throws TypedValueVisitorException
   {
      return constantValue(val, in);
   }
   public O nullConstantValue(ConstantValue.NullConstant val, I in) throws TypedValueVisitorException
   {
      return constantValue(val, in);
   }
   public O stringConstantValue(ConstantValue.StringConstant val, I in) throws TypedValueVisitorException
   {
      return constantValue(val, in);
   }


   public O unaryOpValue(TypedValue.UnaryOperationValue val, I in) throws TypedValueVisitorException
   {
      return defaultValue(val, in);
   }
   public O notOpValue(TypedValue.NotValue val, I in) throws TypedValueVisitorException
   {
      return unaryOpValue(val, in);
   }
   public O castValue(TypedValue.CastValue val, I in) throws TypedValueVisitorException
   {
      return unaryOpValue(val, in);
   }
   public O getFieldValue(TypedValue.GetFieldValue val, I in) throws TypedValueVisitorException
   {
      return unaryOpValue(val, in);
   }

   public O binaryOpValue(TypedValue.BinaryOperationValue val, I in) throws TypedValueVisitorException
   {
      return defaultValue(val, in);
   }
   public O comparisonOpValue(TypedValue.ComparisonValue val, I in) throws TypedValueVisitorException
   {
      return binaryOpValue(val, in);
   }
   public O mathOpValue(TypedValue.MathOpValue val, I in) throws TypedValueVisitorException
   {
      return binaryOpValue(val, in);
   }
   
   public O methodCallValue(MethodCallValue val, I in) throws TypedValueVisitorException
   {
      return defaultValue(val, in);
   }
   public O staticMethodCallValue(MethodCallValue.StaticMethodCallValue val, I in) throws TypedValueVisitorException
   {
      return methodCallValue(val, in);
   }
   public O virtualMethodCallValue(MethodCallValue.VirtualMethodCallValue val, I in) throws TypedValueVisitorException
   {
      return methodCallValue(val, in);
   }

}
