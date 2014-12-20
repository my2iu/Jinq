package org.jinq.jpa.jpqlquery;

public abstract class From implements JPQLFragment
{
   abstract void generateFromString(QueryGenerationState queryState, boolean isFirst);
   public boolean isPrecededByComma() { return true; }

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

   public static FromNavigationalLinksLeftOuterJoinFetch forNavigationalLinksLeftOuterJoinFetch(FromNavigationalLinks link)
   {
      FromNavigationalLinksLeftOuterJoinFetch from = new FromNavigationalLinksLeftOuterJoinFetch();
      from.links = link.links;
      return from;
   }

   public static FromNavigationalLinksJoinFetch forNavigationalLinksJoinFetch(FromNavigationalLinks link)
   {
      FromNavigationalLinksJoinFetch from = new FromNavigationalLinksJoinFetch();
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
   
   public static abstract class FromNavigationalLinksGeneric extends From
   {
      public Expression links;
   }
   
   public static class FromNavigationalLinks extends FromNavigationalLinksGeneric
   {
      @Override
      void generateFromString(QueryGenerationState queryState, boolean isFirst)
      {
         if (!isFirst)
            queryState.queryString += " JOIN ";
         links.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
      }
      @Override public boolean isPrecededByComma() { return false; }
   }

   public static class FromNavigationalLinksLeftOuterJoin extends FromNavigationalLinksGeneric
   {
      @Override
      void generateFromString(QueryGenerationState queryState, boolean isFirst)
      {
         if (!isFirst)
            queryState.queryString += " LEFT OUTER JOIN ";
         links.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
      }
      @Override public boolean isPrecededByComma() { return false; }
   }
   
   public static class FromNavigationalLinksLeftOuterJoinFetch extends FromNavigationalLinksGeneric
   {
      @Override
      void generateFromString(QueryGenerationState queryState, boolean isFirst)
      {
         if (!isFirst)
            queryState.queryString += " LEFT OUTER JOIN FETCH ";
         links.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
      }
      @Override public boolean isPrecededByComma() { return false; }
   }

   public static class FromNavigationalLinksJoinFetch extends FromNavigationalLinksGeneric
   {
      @Override
      void generateFromString(QueryGenerationState queryState, boolean isFirst)
      {
         if (!isFirst)
            queryState.queryString += " JOIN FETCH ";
         links.generateQuery(queryState, OperatorPrecedenceLevel.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
      }
      @Override public boolean isPrecededByComma() { return false; }
   }
}
