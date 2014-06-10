package org.jinq.jooq.transform;

import java.util.List;

import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisMethodChecker;
import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisSupplementalFactory;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue.ComparisonValue;

public class PathAnalysisFactory implements
      PathAnalysisSupplementalFactory<Void, MethodAnalysisResults>
{
   MetamodelUtil metamodel;

   public PathAnalysisFactory(MetamodelUtil metamodel)
   {
      // Build up data structures and other information needed for analysis
      this.metamodel = metamodel;
   }
   
   @Override
   public PathAnalysisMethodChecker createMethodChecker()
   {
      return new MethodChecker(metamodel);
   }

   @Override
   public MethodAnalysisResults createMethodAnalysisResults()
   {
      return new MethodAnalysisResults();
   }

   @Override
   public void addPath(MethodAnalysisResults methodAnalysisResults,
		   TypedValue returnValue, List<ComparisonValue> conditions,
		   PathAnalysisMethodChecker methodChecker) {
	   methodAnalysisResults.addPath(returnValue, conditions);
   }
}
