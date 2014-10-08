package org.jinq.jpa.jpqlquery;

public abstract class From implements JPQLFragment
{
   abstract void generateFromString(QueryGenerationState queryState, boolean isFirst);

   protected void prepareQueryGeneration(Expression.QueryGenerationPreparationPhase preparePhase,
         QueryGenerationState queryState)
   {
      if (preparePhase == Expression.QueryGenerationPreparationPhase.FROM)
      {
         // Generate aliases for each of the FROM entries
         queryState.generateFromAlias(this);
      }
   }
   
   public static From forEntity(String entityName)
   {
      FromEntity from = new FromEntity();
      from.entityName = entityName;
      return from;
   }

   public static From forNavigationalLinks(Expression linksExpr)
   {
      FromNavigationalLinks from = new FromNavigationalLinks();
      from.links = linksExpr;
      return from;
   }

   public static FromNavigationalLinksLeftOuterJoin forNavigationalLinksLeftOuterJoin(FromNavigationalLinks link)
   {
      FromNavigationalLinksLeftOuterJoin from = new FromNavigationalLinksLeftOuterJoin();
      from.links = link.links;
      return from;
   }

   public static class FromEntity extends From
   {
      public String entityName;
      
      @Override
      void generateFromString(QueryGenerationState queryState, boolean isFirst)
      {
         queryState.queryString += entityName;
      }
   }
   
   public static class FromNavigationalLinks extends From
   {
      public Expression links;
      
      @Override
      void generateFromString(QueryGenerationState queryState, boolean isFirst)
      {
         if (!isFirst)
            queryState.queryString += " JOIN ";
         links.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
      }
   }

   public static class FromNavigationalLinksLeftOuterJoin extends From
   {
      public Expression links;
      
      @Override
      void generateFromString(QueryGenerationState queryState, boolean isFirst)
      {
         if (!isFirst)
            queryState.queryString += " LEFT OUTER JOIN ";
         links.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
      }
   }
}
