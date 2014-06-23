package ch.epfl.labos.iu.orm.queryll2.path;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;

public class MethodAnalysisResults
{
   public List<PathAnalysis> paths = new ArrayList<>();

   public void addPath(TypedValue returnValue, List<? extends TypedValue> conditions) {
	      PathAnalysis pathAnalysis = new PathAnalysis(returnValue, conditions);
	      paths.add(pathAnalysis);
   }
}
