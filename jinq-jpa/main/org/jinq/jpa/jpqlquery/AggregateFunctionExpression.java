package org.jinq.jpa.jpqlquery;


public class AggregateFunctionExpression extends Expression
{
   private static final int HASH_SEED = 11;
   private static final int HASH_MULTIPLIER = 37;
   final Expression base;
   final String aggregateName;
   final boolean isDistinct;
   public AggregateFunctionExpression(Expression base, String aggregateName, boolean isDistinct)
   {
      this.base = base;
      this.aggregateName = aggregateName;
      this.isDistinct = isDistinct;
   }
   
   @Override
   public void generateQuery(QueryGenerationState queryState, OperatorPrecedenceLevel operatorPrecedenceScope)
   {
      queryState.appendQuery(aggregateName);
      queryState.appendQuery("(");
      if (isDistinct)
         queryState.appendQuery("DISTINCT ");
      base.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
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
      if (obj == null) return false;
      if (!getClass().equals(obj.getClass())) return false;
      AggregateFunctionExpression o = (AggregateFunctionExpression)obj; 
      return base.equals(o.base) && aggregateName.equals(o.aggregateName) && isDistinct == o.isDistinct;
   }

   @Override
   public int hashCode() {
      int result = HASH_SEED;

      int isDistinctContrib = isDistinct?0:1;
      result = HASH_MULTIPLIER * result + isDistinctContrib;

      int baseContrib = base == null?0:base.hashCode();
      result = HASH_MULTIPLIER * result + baseContrib;

      int aggregateNameContrib = aggregateName == null?0:aggregateName.hashCode();
      return HASH_MULTIPLIER * result + aggregateNameContrib;
   }

   @Override
   public void visit(ExpressionVisitor visitor)
   {
      visitor.visitAggregateFunction(this);
   }
   
   @Override
   public AggregateFunctionExpression copy()
   {
      return new AggregateFunctionExpression(base.copy(), aggregateName, isDistinct);
   }
}
