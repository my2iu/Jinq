package ch.epfl.labos.iu.orm.queryll2.path;

import java.util.List;
import java.util.function.Supplier;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;

public class PathAnalysisFactory implements
      PathAnalysisSupplementalFactory<Void, MethodAnalysisResults>
{
   Supplier<PathAnalysisMethodChecker> methodCheckerFactory;

   public PathAnalysisFactory(Supplier<PathAnalysisMethodChecker> methodCheckerFactory)
   {
      // Build up data structures and other information needed for analysis
      this.methodCheckerFactory = methodCheckerFactory;
   }
   
   @Override
   public PathAnalysisMethodChecker createMethodChecker()
   {
      return methodCheckerFactory.get();
   }

   @Override
   public MethodAnalysisResults createMethodAnalysisResults()
   {
      return new MethodAnalysisResults();
   }

   @Override
   public void addPath(MethodAnalysisResults methodAnalysisResults,
		   TypedValue returnValue, List<? extends TypedValue> conditions,
		   PathAnalysisMethodChecker methodChecker) {
	   methodAnalysisResults.addPath(returnValue, conditions);
   }
}
