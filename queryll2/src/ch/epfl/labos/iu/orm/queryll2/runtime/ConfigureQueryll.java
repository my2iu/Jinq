package ch.epfl.labos.iu.orm.queryll2.runtime;

import ch.epfl.labos.iu.orm.query2.SQLQueryTransforms;

public interface ConfigureQueryll
{
   void configureQueryll(QueryllEntityConfigurationInfo config);
   void storeQueryllAnalysisResults(SQLQueryTransforms queryllAnalysis);
}
