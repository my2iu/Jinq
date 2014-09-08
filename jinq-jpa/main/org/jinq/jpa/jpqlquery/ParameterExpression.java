package org.jinq.jpa.jpqlquery;


public class ParameterExpression extends Expression
{
   private int lambdaIndex;
   private int argIndex;
   
   public ParameterExpression(int lambdaIndex, int argIndex)
   {
      this.lambdaIndex = lambdaIndex;
      this.argIndex = argIndex;
   }
   
   @Override
   public void generateQuery(QueryGenerationState queryState, OperatorPrecedenceLevel operatorPrecedenceScope)
   {
      String paramName = queryState.registerParameter(this, lambdaIndex, argIndex);
      queryState.appendQuery(":" + paramName);
   }
   
   @Override
   public void prepareQueryGeneration(
         QueryGenerationPreparationPhase preparePhase,
         QueryGenerationState queryState)
   {
      // Nothing to do.
   }
   
   @Override public boolean equals(Object obj)
   {
      if (!getClass().equals(obj.getClass())) return false;
      ParameterExpression o = (ParameterExpression)obj; 
      return lambdaIndex == o.lambdaIndex && argIndex == o.argIndex;
   }

   @Override
   public void visit(ExpressionVisitor visitor)
   {
      visitor.visitParameter(this);
   }
}
