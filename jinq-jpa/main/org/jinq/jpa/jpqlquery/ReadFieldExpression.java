package org.jinq.jpa.jpqlquery;


public class ReadFieldExpression extends Expression
{
   public final Expression base;
   public final String field;
   public ReadFieldExpression(Expression base, String field)
   {
      this.base = base;
      this.field = field;
   }
   
   @Override
   public void generateQuery(QueryGenerationState queryState, OperatorPrecedenceLevel operatorPrecedenceScope)
   {
      OperatorPrecedenceLevel precedence = OperatorPrecedenceLevel.forOperator(".");
      if (!precedence.hasPrecedence(operatorPrecedenceScope))
         queryState.appendQuery("(");
      base.generateQuery(queryState, precedence);
      queryState.appendQuery(".");
      queryState.appendQuery(field);
      if (!precedence.hasPrecedence(operatorPrecedenceScope))
         queryState.appendQuery(")");
   }

   @Override
   public void prepareQueryGeneration(
         QueryGenerationPreparationPhase preparePhase,
         QueryGenerationState queryState)
   {
      base.prepareQueryGeneration(preparePhase, queryState);
   }
   
   @Override public boolean equals(Object obj)
   {
      if (!getClass().equals(obj.getClass())) return false;
      ReadFieldExpression o = (ReadFieldExpression)obj; 
      return field.equals(o.field) && base.equals(o.base);
   }

   @Override
   public void visit(ExpressionVisitor visitor)
   {
      visitor.visitReadField(this);
   }
}
