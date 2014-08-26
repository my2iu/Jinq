package org.jinq.jpa.jpqlquery;

import org.jinq.jpa.jpqlquery.Expression.QueryGenerationPreparationPhase;

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
   public void generateQuery(QueryGenerationState queryState, OperatorPrecedenceLevel operatorPrecedenceScope)
   {
      queryState.appendQuery(aggregateName);
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
}
