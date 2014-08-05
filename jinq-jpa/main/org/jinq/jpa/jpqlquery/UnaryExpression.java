package org.jinq.jpa.jpqlquery;

public class UnaryExpression extends Expression
{
   final Expression operand;
   final String operator;
   public UnaryExpression(String operator, Expression operand)
   {
      this.operator = operator;
      this.operand = operand;
   }

   @Override
   public void generateQuery(QueryGenerationState queryState, OperatorPrecedenceLevel operatorPrecedenceScope)
   {
      String precedenceString = operator;
      if (operator.equals("-"))
         precedenceString = "-unary";
      OperatorPrecedenceLevel precedence = OperatorPrecedenceLevel.forOperator(precedenceString);
      if (!precedence.hasPrecedence(operatorPrecedenceScope))
         queryState.appendQuery("(");
      queryState.appendQuery(operator + " ");
      operand.generateQuery(queryState, precedence);

      if (!precedence.hasPrecedence(operatorPrecedenceScope))
         queryState.appendQuery(")");
   }

}
