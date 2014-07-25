package org.jinq.jpa.jpqlquery;

public class AggregateFunctionExpression extends Expression
{
   final Expression base;
   final String aggregateName;
   public AggregateFunctionExpression(Expression base, String aggregateName)
   {
      this.base = base;
      this.aggregateName = aggregateName;
   }
   
   @Override
   public void generateQuery(QueryGenerationState queryState, String operatorPrecedenceScope)
   {
      queryState.appendQuery(aggregateName);
      queryState.appendQuery("(");
      base.generateQuery(queryState, JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
      queryState.appendQuery(")");
   }
}
