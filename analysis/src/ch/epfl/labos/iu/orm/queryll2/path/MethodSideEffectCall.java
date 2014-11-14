package ch.epfl.labos.iu.orm.queryll2.path;

import java.util.List;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;

public class MethodSideEffectCall extends MethodSideEffect
{
   public MethodSignature m;
   public TypedValue base;
   public List<TypedValue> args;
   public MethodSideEffectCall(MethodSignature m, TypedValue base, List<TypedValue> args)
   {
      this.m = m;
      this.base = base;
      this.args = args;
   }

}
