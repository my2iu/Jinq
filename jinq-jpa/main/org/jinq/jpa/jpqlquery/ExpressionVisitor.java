package org.jinq.jpa.jpqlquery;

public class ExpressionVisitor
{
   public void visitDefaultExpression(Expression expr)
   {
   }
   
   public void visitAggregateFunction(
         AggregateFunctionExpression expr)
   {
      visitDefaultExpression(expr);
   }
   
   public void visitBinary(BinaryExpression expr)
   {
      visitDefaultExpression(expr);
   }

   public void visitCaseWhen(CaseWhenExpression expr)
   {
      visitDefaultExpression(expr);
   }

   public void visitConstant(ConstantExpression expr)
   {
      visitDefaultExpression(expr);
   }


   public void visitFromAlias(FromAliasExpression expr)
   {
      visitDefaultExpression(expr);
   }

   public void visitFunction(FunctionExpression expr)
   {
      visitDefaultExpression(expr);
   }

   public void visitParameter(ParameterExpression expr)
   {
      visitDefaultExpression(expr);
   }

   public void visitParameterField(ParameterFieldExpression expr)
   {
      visitDefaultExpression(expr);
   }

   public void visitReadField(ReadFieldExpression expr)
   {
      visitDefaultExpression(expr);
   }

   public void visitSubquery(SubqueryExpression expr)
   {
      visitDefaultExpression(expr);
   }

   public void visitUnary(UnaryExpression expr)
   {
      visitDefaultExpression(expr);
   }

}
