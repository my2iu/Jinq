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
   public void generateQuery(QueryGenerationState queryState, String operatorPrecedenceScope)
   {
      if (!Expression.doesOperatorHaveJPQLPrecedence(operator, operatorPrecedenceScope))
         queryState.appendQuery("(");
      left.generateQuery(queryState, operator);
      queryState.appendQuery(" " + operator + " ");
      // Don't do any operator precedence on the right for now since it requires a more complicated precedence framework
      right.generateQuery(queryState, null);
      if (!Expression.doesOperatorHaveJPQLPrecedence(operator, operatorPrecedenceScope))
         queryState.appendQuery(")");
   }

}
