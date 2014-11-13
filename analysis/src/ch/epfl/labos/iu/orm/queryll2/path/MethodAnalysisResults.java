package ch.epfl.labos.iu.orm.queryll2.path;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;

public class MethodAnalysisResults
{
   public List<PathAnalysis> paths = new ArrayList<>();

   public void addPath(List<MethodSideEffect> sideEffects, TypedValue returnValue, List<? extends TypedValue> conditions) {
	      PathAnalysis pathAnalysis = new PathAnalysis(sideEffects, returnValue, conditions);
	      paths.add(pathAnalysis);
   }
}
