package org.jinq.jpa.jpqlquery;

public abstract class From implements JPQLFragment
{
   abstract void generateFromString(QueryGenerationState queryState);
   
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

   public static class FromEntity extends From
   {
      String entityName;
      void generateFromString(QueryGenerationState queryState)
      {
         queryState.queryString = entityName;
      }
   }
   
   public static class FromNavigationalLinks extends From
   {
      Expression links;
      void generateFromString(QueryGenerationState queryState)
      {
         links.generateQuery(queryState, Expression.JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE);
      }
   }
}
