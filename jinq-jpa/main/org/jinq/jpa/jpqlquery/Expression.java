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
}
