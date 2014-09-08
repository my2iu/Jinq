package org.jinq.jpa.jpqlquery;


public class FunctionExpression extends Expression
{
   Expression base;
   String functionName;
   
   public static FunctionExpression singleParam(String name, Expression base)
   {
      FunctionExpression func = new FunctionExpression();
      func.base = base;
      func.functionName = name;
      return func;
   }
   
   @Override
   public void generateQuery(QueryGenerationState queryState, OperatorPrecedenceLevel operatorPrecedenceScope)
   {
      queryState.appendQuery(functionName);
      queryState.appendQuery("(");
      base.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
      queryState.appendQuery(")");
   }

   @Override
   public void prepareQueryGeneration(
         QueryGenerationPreparationPhase preparePhase,
         QueryGenerationState queryState)
   {
      base.prepareQueryGeneration(preparePhase, queryState);
   }
   
   @Override public boolean equals(Object obj)
   {
      if (!getClass().equals(obj.getClass())) return false;
      FunctionExpression o = (FunctionExpression)obj; 
      return functionName.equals(o.functionName) && base.equals(o.base);
   }

   @Override
   public void visit(ExpressionVisitor visitor)
   {
      visitor.visitFunction(this);
   }
}
