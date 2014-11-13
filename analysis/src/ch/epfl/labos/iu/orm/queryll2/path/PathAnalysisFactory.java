package ch.epfl.labos.iu.orm.queryll2.path;

import java.util.List;
import java.util.function.Supplier;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;

public class PathAnalysisFactory implements
      PathAnalysisSupplementalFactory<Void, MethodAnalysisResults>
{
   PathAnalysisMethodChecker methodChecker;

   public PathAnalysisFactory(PathAnalysisMethodChecker methodChecker)
   {
      // Build up data structures and other information needed for analysis
      this.methodChecker = methodChecker;
   }
   
   @Override
   public PathAnalysisMethodChecker createMethodChecker()
   {
      return methodChecker;
   }

   @Override
   public MethodAnalysisResults createMethodAnalysisResults()
   {
      return new MethodAnalysisResults();
   }

   @Override
   public void addPath(MethodAnalysisResults methodAnalysisResults,
                   List<MethodSideEffect> sideEffects,
		   TypedValue returnValue, List<? extends TypedValue> conditions,
		   PathAnalysisMethodChecker methodChecker) {
	   methodAnalysisResults.addPath(sideEffects, returnValue, conditions);
   }
}
