package org.jinq.jpa.jpqlquery;

public class BinaryExpression extends Expression
{
   final Expression left;
   final Expression right;
   final String operator;
   public BinaryExpression(String operator, Expression left, Expression right)
   {
      this.operator = operator;
      this.left = left;
      this.right = right;
   }

   @Override
   public void generateQuery(QueryGenerationState queryState)
   {
      queryState.appendQuery("(");
      left.generateQuery(queryState);
      queryState.appendQuery(operator);
      right.generateQuery(queryState);
      queryState.appendQuery(")");
   }

}
