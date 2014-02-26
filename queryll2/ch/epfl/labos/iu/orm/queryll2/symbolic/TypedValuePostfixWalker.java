package ch.epfl.labos.iu.orm.queryll2.symbolic;

public class TypedValuePostfixWalker<I,O> extends TypedValueVisitor<I,O>
{
   @Override public O binaryOpValue(TypedValue.BinaryOperationValue val, I in) throws TypedValueVisitorException
   {
      val.left.visit(this, in);
      val.right.visit(this, in);
      return defaultValue(val, in);
   }

   @Override public O unaryOpValue(TypedValue.UnaryOperationValue val, I in) throws TypedValueVisitorException
   {
      val.operand.visit(this, in);
      return defaultValue(val, in);
   }

   @Override public O methodCallValue(MethodCallValue val, I in) throws TypedValueVisitorException
   {
      for (TypedValue arg: val.args)
         arg.visit(this, in);
      return defaultValue(val, in);
   }
   @Override public O staticMethodCallValue(MethodCallValue.StaticMethodCallValue val, I in) throws TypedValueVisitorException
   {
      return methodCallValue(val, in);
   }
   @Override public O virtualMethodCallValue(MethodCallValue.VirtualMethodCallValue val, I in) throws TypedValueVisitorException
   {
      val.base.visit(this, in);
      return methodCallValue(val, in);
   }
}
