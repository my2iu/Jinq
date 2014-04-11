package ch.epfl.labos.iu.orm.queryll2.path;

import java.util.List;

import ch.epfl.labos.iu.orm.queryll2.symbolic.BasicSymbolicInterpreter;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;

public interface PathAnalysisMethodChecker<T> extends BasicSymbolicInterpreter.MethodChecker
{
   public abstract boolean isStaticMethodSafe(MethodSignature m);

   public abstract boolean isMethodSafe(MethodSignature m, TypedValue base,
         List<TypedValue> args);

   public abstract T getSupplementalInfo();

}