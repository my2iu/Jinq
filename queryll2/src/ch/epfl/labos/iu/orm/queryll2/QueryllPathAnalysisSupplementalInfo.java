package ch.epfl.labos.iu.orm.queryll2;

import java.util.Set;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;

public class QueryllPathAnalysisSupplementalInfo
{
   public Set<MethodSignature> transformConstructorsCalled;
   public Set<TypedValue> unresolvedDBSets;
   public Set<TypedValue> unresolvedJinqStreams;
   
   public Set<MethodSignature> getTransformConstructorsCalled()
   {
      return transformConstructorsCalled;
   }
   public Set<TypedValue> getUnresolvedDBSets()
   {
      return unresolvedDBSets;
   }
   public Set<TypedValue> getUnresolvedJinqStreams()
   {
      return unresolvedJinqStreams;
   }
}
