package org.jinq.jpa.transform;

public class ScalaJPQLQueryTransformConfigurationFactory extends JPQLQueryTransformConfigurationFactory
{
   public JPQLQueryTransformConfiguration createConfig()
   {
      return new ScalaJPQLQueryTransformConfiguration();
   }
}
