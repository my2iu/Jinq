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
   
   // Creates a deep copy of the expression. Usually expressions are 
   // immutable, so a copy is needed if you want a version with different
   // parameters etc. This might cause problems with some types of
   // equality testing though. I'm not sure if this is the right approach
   // to allowing renumbering of lambda indices and stuff like that.
   public abstract Expression copy();
}
