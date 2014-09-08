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
      // There's some quirk with precedence levels and ORDER BY requiring that the subquery not be in brackets
      OperatorPrecedenceLevel precedence = OperatorPrecedenceLevel.JPQL_ORDER_BY_UNRESTRICTED_OPERATOR_PRECEDENCE;
      if (!precedence.hasPrecedence(operatorPrecedenceScope))
         queryState.appendQuery("(");
      subquery.generateQueryContents(queryState);
      if (!precedence.hasPrecedence(operatorPrecedenceScope))
         queryState.appendQuery(")");
   }

   @Override
   public void prepareQueryGeneration(
         QueryGenerationPreparationPhase preparePhase,
         QueryGenerationState queryState)
   {
      subquery.prepareQueryGeneration(preparePhase, queryState);
   }
   
   @Override public boolean equals(Object obj)
   {
      if (!getClass().equals(obj.getClass())) return false;
      SubqueryExpression o = (SubqueryExpression)obj;
      // TODO: Recurse into the subquery and test properly for equality
      return subquery.equals(o.subquery);
   }

   @Override
   public void visit(ExpressionVisitor visitor)
   {
      visitor.visitSubquery(this);
   }
}
