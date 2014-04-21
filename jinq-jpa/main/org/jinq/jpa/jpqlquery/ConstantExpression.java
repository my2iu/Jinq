package org.jinq.jpa.jpqlquery;

public class ConstantExpression extends Expression
{
   String text;
   public ConstantExpression(String text)
   {
      this.text = text;
   }
   
   @Override
   public void generateQuery(QueryGenerationState queryState)
   {
      queryState.appendQuery(text);
   }
}
