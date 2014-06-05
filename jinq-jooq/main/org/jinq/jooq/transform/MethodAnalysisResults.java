package org.jinq.jooq.transform;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.labos.iu.orm.queryll2.symbolic.ConstantValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue.ComparisonValue;

public class MethodAnalysisResults
{
   public List<PathAnalysis> paths = new ArrayList<>();

   public void addPath(TypedValue returnValue, List<ComparisonValue> conditions) {
	      PathAnalysis pathAnalysis = new PathAnalysis(returnValue, conditions);
	      paths.add(pathAnalysis);
   }
   
   /**
    * Sometimes paths have weird conditions on them, so they can be pruned out.
    * (e.g. paths where the only condition is TRUE or FALSE)
    */
   public void cleanPaths()
   {
      List<PathAnalysis> pathsToDelete = new ArrayList<>();
      for (PathAnalysis path: paths)
      {
         List<TypedValue> conditionsToDelete = new ArrayList<>();
         int n = 0;
         for (TypedValue val: path.getSimplifiedBooleanConditions())
         {
            if (val instanceof ConstantValue.BooleanConstant)
            {
               if (((ConstantValue.BooleanConstant)val).getConstant())
               {
                  // This part of the path condition is always TRUE, so it's
                  // redundant and can remove it as a path condition
                  conditionsToDelete.add(path.conditions.get(n));
               }
               else
               {
                  // One part of the path condition is FALSE, so the path condition
                  // can never hold. Prune out the whole path
                  pathsToDelete.add(path);
               }
            }
            n++;
         }
         path.conditions.removeAll(conditionsToDelete);
      }
      paths.removeAll(pathsToDelete);
   }
}
