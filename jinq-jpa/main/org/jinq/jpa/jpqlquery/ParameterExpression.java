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
   public void generateQuery(QueryGenerationState queryState)
   {
      String paramName = queryState.registerParameter(this, lambdaIndex, argIndex);
      queryState.appendQuery(":" + paramName);
   }
}
