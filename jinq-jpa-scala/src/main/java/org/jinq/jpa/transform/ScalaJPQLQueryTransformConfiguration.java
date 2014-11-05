package org.jinq.jpa.transform;

public class ScalaJPQLQueryTransformConfiguration extends
      JPQLQueryTransformConfiguration
{
   @Override
   public SymbExToColumns newSymbExToColumns(
         SymbExArgumentHandler argumentHandler)
   {
      return new ScalaSymbExToColumns(this, argumentHandler);
   }

   @Override
   public SymbExToSubQuery newSymbExToSubQuery(
         SymbExArgumentHandler argumentHandler)
   {
      return new ScalaSymbExToSubQuery(this, argumentHandler);
   }
}
