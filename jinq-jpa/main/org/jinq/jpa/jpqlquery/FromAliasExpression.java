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
}
