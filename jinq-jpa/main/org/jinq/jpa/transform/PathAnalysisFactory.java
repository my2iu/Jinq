package org.jinq.jpa.transform;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jinq.jpa.MetamodelUtil;

import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisMethodChecker;
import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisSupplementalFactory;
import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue.ComparisonValue;

public class PathAnalysisFactory implements
      PathAnalysisSupplementalFactory<Void, MethodAnalysisResults>
{
   private final Set<Class<?>> safeMethodAnnotations;
   private final Set<MethodSignature> safeMethods;
   private final Set<MethodSignature> safeStaticMethods;

   public PathAnalysisFactory(MetamodelUtil metamodel)
   {
      // Build up data structures and other information needed for analysis
      safeMethodAnnotations = metamodel.safeMethodAnnotations;
      safeMethods = metamodel.safeMethods;
      safeStaticMethods = metamodel.safeStaticMethods;
   }
   
   @Override
   public PathAnalysisMethodChecker createMethodChecker()
   {
      return new MethodChecker(
            safeMethodAnnotations, 
            safeMethods, safeStaticMethods);
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
