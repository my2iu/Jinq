package org.jinq.jpa.jpqlquery;


public class ConstantExpression extends Expression
{
   String text;
   public ConstantExpression(String text)
   {
      this.text = text;
   }
   
   @Override
   public void generateQuery(QueryGenerationState queryState, OperatorPrecedenceLevel operatorPrecedenceScope)
   {
      queryState.appendQuery(text);
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
      ConstantExpression o = (ConstantExpression)obj; 
      return text.equals(o.text);
   }

   @Override
   public void visit(ExpressionVisitor visitor)
   {
      visitor.visitConstant(this);
   }
}
