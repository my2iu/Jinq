package org.jinq.jpa.jpqlquery;

import java.util.List;


public class CaseWhenExpression extends Expression
{
   public static class ConditionResult
   {
      public Expression condition;
      public Expression result;
   }
   final List<ConditionResult> cases;
   
   public CaseWhenExpression(List<ConditionResult> cases)
   {
      this.cases = cases;
   }

   @Override
   public void generateQuery(QueryGenerationState queryState, OperatorPrecedenceLevel operatorPrecedenceScope)
   {
      queryState.appendQuery("CASE");
      for (int n = 0; n < cases.size() - 1; n++)
      {
         queryState.appendQuery(" WHEN ");
         cases.get(n).condition.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
         queryState.appendQuery(" THEN ");
         cases.get(n).result.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
      }
      queryState.appendQuery(" ELSE ");
      cases.get(cases.size() - 1).result.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
      queryState.appendQuery(" END");
   }

   @Override
   public void prepareQueryGeneration(
         QueryGenerationPreparationPhase preparePhase,
         QueryGenerationState queryState)
   {
      for (ConditionResult c: cases)
      {
         c.condition.prepareQueryGeneration(preparePhase, queryState);
         c.result.prepareQueryGeneration(preparePhase, queryState);
      }
   }
   
   @Override public boolean equals(Object obj)
   {
      if (!getClass().equals(obj.getClass())) return false;
      CaseWhenExpression o = (CaseWhenExpression)obj; 
      if (cases.size() != o.cases.size()) return false;
      for (int n = 0; n < cases.size(); n++)
         if (!cases.get(n).condition.equals(o.cases.get(n).condition) || !cases.get(n).result.equals(o.cases.get(n).result))
            return false;
      return true;
   }

   @Override
   public void visit(ExpressionVisitor visitor)
   {
      visitor.visitCaseWhen(this);
   }
}
