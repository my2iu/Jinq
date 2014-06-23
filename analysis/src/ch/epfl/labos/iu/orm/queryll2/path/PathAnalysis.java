package ch.epfl.labos.iu.orm.queryll2.path;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;

public class PathAnalysis
{
   // TODO: Doing simplification of expressions at this stage using a rewriter
   // is ok, but it does mean that you can't simplify across nested queries
   // which may be necessary if a nested query has a parameter which refers
   // to something in the parent query, meaning it could be substituted in
   // and simplified

   
   public PathAnalysis(TypedValue returnValue, 
                       List<? extends TypedValue> conditions)
   {
      this.returnValue = returnValue;
      this.conditions = new ArrayList<>(conditions);
   }
   
   public TypedValue getReturnValue()
   {
      return returnValue;
   }
   public List<TypedValue> getConditions()
   {
      return conditions;
   }
   TypedValue returnValue;
   List<TypedValue> conditions;
   public void removeConditionIndex(int i)
   {
      conditions.remove(i);
   }
}
