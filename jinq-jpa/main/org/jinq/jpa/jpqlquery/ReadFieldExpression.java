package org.jinq.jpa.jpqlquery;


public class ReadFieldExpression extends Expression
{
   final Expression base;
   final String field;
   public ReadFieldExpression(Expression base, String field)
   {
      this.base = base;
      this.field = field;
   }
   
   @Override
   public void generateQuery(QueryGenerationState queryState, OperatorPrecedenceLevel operatorPrecedenceScope)
   {
      OperatorPrecedenceLevel precedence = OperatorPrecedenceLevel.forOperator(".");
      if (!precedence.hasPrecedence(operatorPrecedenceScope))
         queryState.appendQuery("(");
      base.generateQuery(queryState, precedence);
      queryState.appendQuery(".");
      queryState.appendQuery(field);
      if (!precedence.hasPrecedence(operatorPrecedenceScope))
         queryState.appendQuery(")");
   }

   @Override
   public void prepareQueryGeneration(
         QueryGenerationPreparationPhase preparePhase,
         QueryGenerationState queryState)
   {
      base.prepareQueryGeneration(preparePhase, queryState);
   }
}
