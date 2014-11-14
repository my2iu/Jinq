package ch.epfl.labos.iu.orm.queryll2.path;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;

public class MethodSideEffectFieldAssign extends MethodSideEffect
{
   public MethodSideEffectFieldAssign(String owner, String name, String desc,
         TypedValue base, TypedValue value)
   {
      this.owner = owner;
      this.name = name;
      this.desc = desc;
      this.base = base;
      this.value = value;
   }

   public String owner;
   public String name;
   public String desc;
   public TypedValue base;
   public TypedValue value;
}
