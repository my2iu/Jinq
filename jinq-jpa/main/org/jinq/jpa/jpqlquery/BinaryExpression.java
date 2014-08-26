package org.jinq.jpa.jpqlquery;

import org.jinq.jpa.jpqlquery.Expression.QueryGenerationPreparationPhase;

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
   public void generateQuery(QueryGenerationState queryState, OperatorPrecedenceLevel operatorPrecedenceScope)
   {
      OperatorPrecedenceLevel precedence = OperatorPrecedenceLevel.forOperator(operator);
      if (!precedence.hasPrecedence(operatorPrecedenceScope))
         queryState.appendQuery("(");
      left.generateQuery(queryState, precedence);
      queryState.appendQuery(" " + operator + " ");
      // Things on the right hand side should be wrapped in brackets if they are at the same precedence level
      // so we need to pass down a lower precedence scope there.
      right.generateQuery(queryState, precedence.getLevelBelow());
      if (!precedence.hasPrecedence(operatorPrecedenceScope))
         queryState.appendQuery(")");
   }

   @Override
   public void prepareQueryGeneration(
         QueryGenerationPreparationPhase preparePhase,
         QueryGenerationState queryState)
   {
      left.prepareQueryGeneration(preparePhase, queryState);
      right.prepareQueryGeneration(preparePhase, queryState);
   }
}
