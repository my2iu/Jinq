package org.jinq.jpa.jpqlquery;

public class RecursiveExpressionVisitor extends ExpressionVisitor
{
   @Override
   public void visitAggregateFunction(
         AggregateFunctionExpression expr)
   {
      expr.base.visit(this);
      super.visitAggregateFunction(expr);
   }
   
   @Override
   public void visitBinary(BinaryExpression expr)
   {
      expr.left.visit(this);
      expr.right.visit(this);
      super.visitBinary(expr);
   }

   @Override
   public void visitCaseWhen(CaseWhenExpression expr)
   {
      for (CaseWhenExpression.ConditionResult c: expr.cases)
      {
         c.condition.visit(this);
         c.result.visit(this);
      }
      super.visitCaseWhen(expr);
   }
   
   @Override
   public void visitConstant(ConstantExpression expr)
   {
      super.visitConstant(expr);
   }


   @Override
   public void visitFromAlias(FromAliasExpression expr)
   {
      super.visitFromAlias(expr);
   }

   @Override
   public void visitFunction(FunctionExpression expr)
   {
      for (Expression arg: expr.arguments)
         arg.visit(this);
      super.visitFunction(expr);
   }

   @Override
   public void visitParameter(ParameterExpression expr)
   {
      super.visitParameter(expr);
   }
   
   @Override
   public void visitParameterField(ParameterFieldExpression expr)
   {
      super.visitParameterField(expr);
   }

   @Override
   public void visitReadField(ReadFieldExpression expr)
   {
      expr.base.visit(this);
      super.visitReadField(expr);
   }

   @Override
   public void visitSubquery(SubqueryExpression expr)
   {
      super.visitSubquery(expr);
   }

   @Override
   public void visitUnary(UnaryExpression expr)
   {
      expr.operand.visit(this);
      super.visitUnary(expr);
   }

}
