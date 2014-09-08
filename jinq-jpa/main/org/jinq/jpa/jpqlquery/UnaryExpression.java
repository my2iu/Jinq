package org.jinq.jpa.jpqlquery;


public class UnaryExpression extends Expression
{
   final Expression operand;
   final String prefixOperator;
   final String postfixOperator;
   private UnaryExpression(String prefixOperator, String postfixOperator, Expression operand)
   {
      this.prefixOperator = prefixOperator;
      this.postfixOperator = postfixOperator;
      this.operand = operand;
   }
   
   public static UnaryExpression prefix(String operator, Expression operand)
   {
      return new UnaryExpression(operator, "", operand);
   }

   public static UnaryExpression postfix(String operator, Expression operand)
   {
      return new UnaryExpression("", operator, operand);
   }

   @Override
   public void generateQuery(QueryGenerationState queryState, OperatorPrecedenceLevel operatorPrecedenceScope)
   {
      String operator = prefixOperator + postfixOperator;
      String precedenceString = operator;
      if (operator.equals("-"))
         precedenceString = "-unary";
      OperatorPrecedenceLevel precedence = OperatorPrecedenceLevel.forOperator(precedenceString);
      if (!precedence.hasPrecedence(operatorPrecedenceScope))
         queryState.appendQuery("(");
      if (!prefixOperator.isEmpty())
         queryState.appendQuery(prefixOperator + " ");
      operand.generateQuery(queryState, precedence);
      if (!postfixOperator.isEmpty())
         queryState.appendQuery(" " + postfixOperator);

      if (!precedence.hasPrecedence(operatorPrecedenceScope))
         queryState.appendQuery(")");
   }

   @Override
   public void prepareQueryGeneration(
         QueryGenerationPreparationPhase preparePhase,
         QueryGenerationState queryState)
   {
      operand.prepareQueryGeneration(preparePhase, queryState);
   }
   
   @Override public boolean equals(Object obj)
   {
      if (!getClass().equals(obj.getClass())) return false;
      UnaryExpression o = (UnaryExpression)obj; 
      return operand.equals(o.operand) && prefixOperator.equals(o.prefixOperator) && postfixOperator.equals(o.postfixOperator);
   }

   @Override
   public void visit(ExpressionVisitor visitor)
   {
      visitor.visitUnary(this);
   }
}
