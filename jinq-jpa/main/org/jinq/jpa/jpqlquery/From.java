package org.jinq.jpa.jpqlquery;

public abstract class From implements JPQLFragment
{
   abstract void generateFromString(QueryGenerationState queryState);

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
      String entityName;
      void generateFromString(QueryGenerationState queryState)
      {
         queryState.queryString += entityName;
      }
   }
   
   public static class FromNavigationalLinks extends From
   {
      Expression links;
      void generateFromString(QueryGenerationState queryState)
      {
         links.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
      }
   }

   public static class FromNavigationalLinksLeftOuterJoin extends From
   {
      Expression links;
      void generateFromString(QueryGenerationState queryState)
      {
         links.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
      }
   }
}
