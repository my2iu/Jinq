package org.jinq.jpa.jpqlquery;

public class FromAliasExpression extends Expression
{
   public From from;
   public FromAliasExpression(From from)
   {
      this.from = from;
   }
   
   @Override
   public void generateQuery(QueryGenerationState queryState, OperatorPrecedenceLevel operatorPrecedenceScope)
   {
      queryState.appendQuery(queryState.getFromAlias(from));
   }
   
   @Override
   public void prepareQueryGeneration(
         QueryGenerationPreparationPhase preparePhase,
         QueryGenerationState queryState)
   {
      // Nothing to do.
   }
   
   @Override public boolean equals(Object obj)
   {
      if (!getClass().equals(obj.getClass())) return false;
      FromAliasExpression o = (FromAliasExpression)obj; 
      return from.equals(o.from);
   }

   @Override
   public void visit(ExpressionVisitor visitor)
   {
      visitor.visitFromAlias(this);
   }
}
