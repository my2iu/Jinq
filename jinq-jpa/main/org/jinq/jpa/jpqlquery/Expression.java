package org.jinq.jpa.jpqlquery;


public abstract class Expression
{
   enum QueryGenerationPreparationPhase
   {
      FROM
   }
   public void generateQuery(QueryGenerationState queryState, OperatorPrecedenceLevel operatorPrecedenceScope)
   {
      
   }
   
   public abstract void prepareQueryGeneration(QueryGenerationPreparationPhase preparePhase, QueryGenerationState queryState);
   
   public abstract void visit(ExpressionVisitor visitor);
   
   @Override public boolean equals(Object obj)
   {
      if (!getClass().equals(obj.getClass())) return false;
      return true;
   }
}
