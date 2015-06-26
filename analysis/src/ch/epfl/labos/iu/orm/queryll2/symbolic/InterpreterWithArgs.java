package ch.epfl.labos.iu.orm.queryll2.symbolic;

import org.jinq.rebased.org.objectweb.asm.Type;
import org.jinq.rebased.org.objectweb.asm.tree.analysis.Interpreter;
import org.jinq.rebased.org.objectweb.asm.tree.analysis.Value;

// This is an extended version of the Interpreter interface that
// allows the symbolic executor to keep track of method arguments

public abstract class InterpreterWithArgs<V extends Value> extends Interpreter
{
   protected InterpreterWithArgs(int api)
  {
    super(api);
  }

  // initializes a "this" variable
   public abstract Value newThis(Type type);
   
   // initializes a variable referring to a method argument
   public abstract Value newArg(Type type, int argumentIndex);

}
