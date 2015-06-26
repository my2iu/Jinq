package ch.epfl.labos.iu.orm.queryll2.symbolic;

import org.jinq.rebased.org.objectweb.asm.tree.AbstractInsnNode;
import org.jinq.rebased.org.objectweb.asm.tree.FieldInsnNode;
import org.jinq.rebased.org.objectweb.asm.tree.analysis.AnalyzerException;
import org.jinq.rebased.org.objectweb.asm.tree.analysis.Value;

import ch.epfl.labos.iu.orm.queryll2.path.MethodSideEffectFieldAssign;


public class SymbolicInterpreterWithFieldAccess extends BasicSymbolicInterpreter
{
   public SymbolicInterpreterWithFieldAccess(int api)
  {
    super(api);
  }

  private TypedValue createGetFieldTypedValue(AbstractInsnNode insn, Value base)
   {
      FieldInsnNode fieldInsn = (FieldInsnNode)insn;
      return new TypedValue.GetFieldValue(fieldInsn.owner, fieldInsn.name, fieldInsn.desc, (TypedValue)base);
   }
   
   public Value binaryOperation(AbstractInsnNode insn, Value value1,
         Value value2) throws AnalyzerException
   {
      if (insn.getOpcode() == PUTFIELD)
      {
         if (value1 instanceof TypedValue.ThisValue)
         {
            if (methodChecker != null && methodChecker.isPutFieldAllowed())
            {
               FieldInsnNode fieldInsn = (FieldInsnNode)insn;
               sideEffects.add(new MethodSideEffectFieldAssign(fieldInsn.owner, fieldInsn.name, fieldInsn.desc, (TypedValue)value1, (TypedValue)value2));
               return null;
            }
         }
      }
      return super.binaryOperation(insn, value1, value2);
   }

   public Value unaryOperation(AbstractInsnNode insn, Value value)
      throws AnalyzerException
   {
      if (insn.getOpcode() == GETFIELD)
      {
         if (value instanceof TypedValue.ThisValue)
            return createGetFieldTypedValue(insn, value);
         else if (value instanceof TypedValue.GetFieldValue)
         {
            // We can handle getting the field of a field of this etc.
            TypedValue baseVal = (TypedValue)value;
            while (baseVal instanceof TypedValue.GetFieldValue)
               baseVal = ((TypedValue.GetFieldValue)baseVal).operand;
            if (baseVal instanceof  TypedValue.ThisValue)
               return createGetFieldTypedValue(insn, value);
         }
         throw new AnalyzerException(insn, "Unhandled field access");
      }
      else
         return super.unaryOperation(insn, value);
   }

}
