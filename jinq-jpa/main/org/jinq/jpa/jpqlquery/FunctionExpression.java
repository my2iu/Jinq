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
}
