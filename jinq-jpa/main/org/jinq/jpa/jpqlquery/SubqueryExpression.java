package org.jinq.jpa.jpqlquery;


public class SubqueryExpression extends Expression
{
   SelectFromWhere<?> subquery;
   
   public static SubqueryExpression from(SelectFromWhere<?> subquery)
   {
      SubqueryExpression expr = new SubqueryExpression();
      expr.subquery = subquery;
      return expr;
   }

   @Override
   public void generateQuery(QueryGenerationState queryState, OperatorPrecedenceLevel operatorPrecedenceScope)
   {
      queryState.appendQuery("(");
      subquery.generateQueryContents(queryState);
      queryState.appendQuery(")");
      
   }

   @Override
   public void prepareQueryGeneration(
         QueryGenerationPreparationPhase preparePhase,
         QueryGenerationState queryState)
   {
      subquery.prepareQueryGeneration(preparePhase, queryState);
   }
}
