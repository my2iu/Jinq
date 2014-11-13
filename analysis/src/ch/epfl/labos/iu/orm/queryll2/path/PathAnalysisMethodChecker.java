package ch.epfl.labos.iu.orm.queryll2.path;

import java.util.List;

import ch.epfl.labos.iu.orm.queryll2.symbolic.BasicSymbolicInterpreter;
import ch.epfl.labos.iu.orm.queryll2.symbolic.BasicSymbolicInterpreter.OperationSideEffect;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;

public interface PathAnalysisMethodChecker extends BasicSymbolicInterpreter.MethodChecker
{
   public abstract OperationSideEffect isStaticMethodSafe(MethodSignature m);

   public abstract OperationSideEffect isMethodSafe(MethodSignature m, TypedValue base,
         List<TypedValue> args);
}