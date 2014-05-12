package org.jinq.jpa.jpqlquery;

public class ReadFieldExpression extends Expression
{
   final Expression base;
   final String field;
   public ReadFieldExpression(Expression base, String field)
   {
      this.base = base;
      this.field = field;
   }
   
   @Override
   public void generateQuery(QueryGenerationState queryState, String operatorPrecedenceScope)
   {
      if (!Expression.doesOperatorHaveJPQLPrecedence(".", operatorPrecedenceScope))
         queryState.appendQuery("(");
      base.generateQuery(queryState, ".");
      queryState.appendQuery(".");
      queryState.appendQuery(field);
      if (!Expression.doesOperatorHaveJPQLPrecedence(".", operatorPrecedenceScope))
         queryState.appendQuery(")");
   }
}
