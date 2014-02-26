package ch.epfl.labos.iu.orm.queryll2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;

public class MethodAnalysisResults
{
   public List<PathAnalysis> paths;
   
   public Set<MethodSignature> getAllTransformConstructorsCalled()
   {
      Set<MethodSignature> allTransforms = new HashSet<MethodSignature>();
      for (PathAnalysis path: paths)
         allTransforms.addAll(path.getTransformConstructorsCalled());
      return allTransforms;
   }
   
   public Set<TypedValue> getAllUnresolvedDBSets()
   {
      Set<TypedValue> allUnresolvedDBSets = new HashSet<TypedValue>();
      for (PathAnalysis path: paths)
         allUnresolvedDBSets.addAll(path.getUnresolvedDBSets());
      return allUnresolvedDBSets;
   }
}
