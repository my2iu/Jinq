package ch.epfl.labos.iu.orm.queryll2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisMethodChecker;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;

public class MethodAnalysisResults extends ch.epfl.labos.iu.orm.queryll2.path.MethodAnalysisResultsHolder<QueryllPathAnalysisSupplementalInfo>
{
   public List<PathAnalysis<QueryllPathAnalysisSupplementalInfo>> paths = new ArrayList<>();

   @Override
   public void addPath(TypedValue returnValue, 
               List<TypedValue.ComparisonValue> conditions,
               PathAnalysisMethodChecker methodChecker)
   {
      PathAnalysis<QueryllPathAnalysisSupplementalInfo> pathAnalysis = new PathAnalysis<>(returnValue, conditions);
      pathAnalysis.setSupplementalInfo(((QueryllMethodChecker)methodChecker).getSupplementalInfo());
      paths.add(pathAnalysis);
   }
   // TODO: No one uses these, but they will be needed in the future for more complicated analyses
//   public Set<MethodSignature> getAllTransformConstructorsCalled()
//   {
//      Set<MethodSignature> allTransforms = new HashSet<MethodSignature>();
//      for (PathAnalysis<QueryllPathAnalysisSupplementalInfo> path: paths)
//         allTransforms.addAll(path.getSupplementalInfo().getTransformConstructorsCalled());
//      return allTransforms;
//   }
//   
//   public Set<TypedValue> getAllUnresolvedDBSets()
//   {
//      Set<TypedValue> allUnresolvedDBSets = new HashSet<TypedValue>();
//      for (PathAnalysis<QueryllPathAnalysisSupplementalInfo> path: paths)
//         allUnresolvedDBSets.addAll(path.getSupplementalInfo().getUnresolvedDBSets());
//      return allUnresolvedDBSets;
//   }
//
//   public Set<TypedValue> getAllUnresolvedJinqStreams()
//   {
//      Set<TypedValue> allUnresolvedJinqStreams = new HashSet<TypedValue>();
//      for (PathAnalysis<QueryllPathAnalysisSupplementalInfo> path: paths)
//         allUnresolvedJinqStreams.addAll(path.getSupplementalInfo().getUnresolvedJinqStreams());
//      return allUnresolvedJinqStreams;
//   }
}
