package ch.epfl.labos.iu.orm.queryll2.symbolic;

public abstract class TypedValuePostfixWalker<I,O, E extends Exception> extends TypedValueVisitor<I,O,E>
{
   @Override public O binaryOpValue(TypedValue.BinaryOperationValue val, I in) throws E
   {
      val.left.visit(this, in);
      val.right.visit(this, in);
      return defaultValue(val, in);
   }

   @Override public O unaryOpValue(TypedValue.UnaryOperationValue val, I in) throws E
   {
      val.operand.visit(this, in);
      return defaultValue(val, in);
   }

   @Override public O methodCallValue(MethodCallValue val, I in) throws E
   {
      for (TypedValue arg: val.args)
         arg.visit(this, in);
      return defaultValue(val, in);
   }
   @Override public O staticMethodCallValue(MethodCallValue.StaticMethodCallValue val, I in) throws E
   {
      return methodCallValue(val, in);
   }
   @Override public O virtualMethodCallValue(MethodCallValue.VirtualMethodCallValue val, I in) throws E
   {
      val.base.visit(this, in);
      return methodCallValue(val, in);
   }
}
