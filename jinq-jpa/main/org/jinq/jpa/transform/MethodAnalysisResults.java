package org.jinq.jpa.transform;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue.ComparisonValue;

public class MethodAnalysisResults
{
   public List<PathAnalysis> paths = new ArrayList<>();

   public void addPath(TypedValue returnValue, List<ComparisonValue> conditions) {
	      PathAnalysis pathAnalysis = new PathAnalysis(returnValue, conditions);
	      paths.add(pathAnalysis);
   }
}
