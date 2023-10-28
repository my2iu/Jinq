package org.jinq.jpa.jpqlquery;

/**
 * Adjusts the lambda indices on some expressions. Since Expression
 * objects are supposed to be immutable, be sure you know what you're
 * doing if you use this!
 */
public class OffsetLambdaIndexInExpressionsVisitor extends RecursiveExpressionVisitor
{
   int offset;
   public OffsetLambdaIndexInExpressionsVisitor(int offset)
   {
      this.offset = offset;
   }
   
   @Override
   public void visitParameter(ParameterExpression expr)
   {
      expr.offsetLambdaIndices(offset);
   }
   
   @Override
   public void visitParameterField(ParameterFieldExpression expr)
   {
      expr.offsetLambdaIndices(offset);
   }
}
