package ch.epfl.labos.iu.orm.queryll2.symbolic;

import java.util.ArrayList;
import java.util.List;

import org.jinq.rebased.org.objectweb.asm.Handle;
import org.jinq.rebased.org.objectweb.asm.Opcodes;
import org.jinq.rebased.org.objectweb.asm.Type;
import org.jinq.rebased.org.objectweb.asm.tree.AbstractInsnNode;
import org.jinq.rebased.org.objectweb.asm.tree.FieldInsnNode;
import org.jinq.rebased.org.objectweb.asm.tree.IntInsnNode;
import org.jinq.rebased.org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.jinq.rebased.org.objectweb.asm.tree.LdcInsnNode;
import org.jinq.rebased.org.objectweb.asm.tree.MethodInsnNode;
import org.jinq.rebased.org.objectweb.asm.tree.TypeInsnNode;
import org.jinq.rebased.org.objectweb.asm.tree.analysis.AnalyzerException;
import org.jinq.rebased.org.objectweb.asm.tree.analysis.Value;

import ch.epfl.labos.iu.orm.queryll2.path.MethodSideEffect;
import ch.epfl.labos.iu.orm.queryll2.path.MethodSideEffectCall;


public class BasicSymbolicInterpreter extends InterpreterWithArgs implements Opcodes
{
   public BasicSymbolicInterpreter(int api)
  {
    super(api);
  }

  // Our symbolic execution needs to handle simple aliasing in order to
   // treat constructors properly. The ASM framework doesn't allow the 
   // interpreter to change the values of variables other than the ones
   // that are being manipulated by an instruction, but to do aliasing
   // we need to reach in the the stack frame and change those other
   // variables, so we need this frame of variables.
   FrameWithHelpers linkedFrame;
   public void setFrameForAliasingFixups(FrameWithHelpers frame)
   {
      linkedFrame = frame;
   }

   // Keeps track of conditions that must hold for instructions to be executed
   public static interface BranchHandler
   {
      void ifInstruction(AbstractInsnNode insn, TypedValue.ComparisonValue ifTrueValue);
   }
   BranchHandler branchHandler;
   public void setBranchHandler(BranchHandler branchHandler)
   {
      this.branchHandler = branchHandler;
   }
   
   // Keeps track of return value from a path
   public TypedValue returnValue = null;
   
   // Keeps track of other side effects from executing a path
   public List<MethodSideEffect> sideEffects = new ArrayList<>();

   // Keeps track of which methods can be safely called
   public static enum OperationSideEffect
   {
      NONE,   // No side effects 
      UNSAFE, // Has unsafe side effects, cancel the analysis
      SAFE    // There are side effects, but they should be safe. Log the side effect
   }
   public static interface MethodChecker
   {
      OperationSideEffect isMethodSafe(MethodSignature m, TypedValue base, List<TypedValue> args);
      OperationSideEffect isStaticMethodSafe(MethodSignature m);
      boolean isFluentChaining(MethodSignature m);
      boolean isPutFieldAllowed();
   }
   MethodChecker methodChecker;
   public void setMethodChecker(MethodChecker methodChecker)
   {
      this.methodChecker = methodChecker;
   }
   
   // Stuff that is called during the symbolic interpretation by Frame
   
   public Value newArg(Type type, int argumentIndex)
   {
      return new TypedValue.ArgValue(type, argumentIndex);
   }

   public Value newThis(Type type)
   {
      return new TypedValue.ThisValue(type);
   }

   public Value newValue(Type type)
   {
      // In practice, this will only be called with type==null for
      // uninitialized stack slot entries
      assert(type == null);
      return new TypedValue(type);
   }

   public Value copyOperation(AbstractInsnNode insn, Value value)
         throws AnalyzerException
   {
      return value;
   }

   public Value merge(Value v, Value w)
   {
      // not used
      return null;
   }

   public Value newOperation(AbstractInsnNode insn) throws AnalyzerException
   {
      switch (insn.getOpcode())
      {
         case ICONST_M1:
         case ICONST_0:
         case ICONST_1:
         case ICONST_2:
         case ICONST_3:
         case ICONST_4:
         case ICONST_5:
            return new ConstantValue.IntegerConstant(insn.getOpcode() - ICONST_M1 - 1);
         case LCONST_0:
         case LCONST_1:
            return new ConstantValue.LongConstant(insn.getOpcode() - LCONST_0);
         case FCONST_0:
         case FCONST_1:
         case FCONST_2:
            return new ConstantValue.FloatConstant(insn.getOpcode() - FCONST_0);
         case DCONST_0:
         case DCONST_1:
            return new ConstantValue.DoubleConstant(insn.getOpcode() - DCONST_0);
         case ACONST_NULL:
            return new ConstantValue.NullConstant();
         case LDC:
         {
            assert(insn instanceof LdcInsnNode);
            LdcInsnNode ldcInsn = (LdcInsnNode)insn;
            Object val = ldcInsn.cst;
            if (val instanceof String)
               return new ConstantValue.StringConstant((String)val);
            else if (val instanceof Integer)
               return new ConstantValue.IntegerConstant(((Integer)val).intValue());
            else if (val instanceof Long)
               return new ConstantValue.LongConstant(((Long)val).longValue());
            else if (val instanceof Float)
               return new ConstantValue.FloatConstant(((Float)val).floatValue());
            else if (val instanceof Double)
               return new ConstantValue.DoubleConstant(((Double)val).doubleValue());
            else if (val instanceof Type)
               return new ConstantValue.ClassConstant((Type)val);
            else
               throw new AnalyzerException(insn, "Unhandled bytecode instruction");
         }
         case NEW:
         {
            assert(insn instanceof TypeInsnNode);
            TypeInsnNode typeInsn = (TypeInsnNode)insn;
            String className = typeInsn.desc;
            return new TypedValue.NewValue(className);
         }
         case BIPUSH:
         case SIPUSH:
         {
            assert(insn instanceof IntInsnNode);
            IntInsnNode intInsn = (IntInsnNode)insn;
            return new ConstantValue.IntegerConstant(intInsn.operand);
         }
         case GETSTATIC:
         {
            assert(insn instanceof FieldInsnNode);
            FieldInsnNode fieldInsn = (FieldInsnNode)insn;
            return new TypedValue.GetStaticFieldValue(fieldInsn.owner, fieldInsn.name, fieldInsn.desc);
         }
         case JSR:
         default:
            throw new AnalyzerException(insn, "Unhandled bytecode instruction");
      }
   }

   public Value unaryOperation(AbstractInsnNode insn, Value value)
      throws AnalyzerException
   {
      switch(insn.getOpcode())
      {
         case IRETURN: 
         case LRETURN: 
         case FRETURN: 
         case DRETURN: 
         case ARETURN:
            // Return instructions will be handled in returnOperation() 
            return null;
         case IFEQ: 
         case IFNE:
         case IFLT: 
         case IFGE: 
         case IFGT: 
         case IFLE:
            return ifOperation(insn, value, new ConstantValue.IntegerConstant(0));
         case IFNULL:
         case IFNONNULL:
            return ifOperation(insn, value, new ConstantValue.NullConstant());
         case CHECKCAST:
         {
            TypeInsnNode typeInsn = (TypeInsnNode)insn;
            return new TypedValue.CastValue(Type.getObjectType(typeInsn.desc), (TypedValue)value);
         }
         case I2L:
         case F2L:
         case D2L:
            return new TypedValue.CastValue(Type.LONG_TYPE, (TypedValue)value);
         case L2I:
         case F2I:
         case D2I:
            return new TypedValue.CastValue(Type.INT_TYPE, (TypedValue)value);
         case I2F:
         case L2F:
         case D2F:
            return new TypedValue.CastValue(Type.FLOAT_TYPE, (TypedValue)value);
         case I2D:
         case L2D:
         case F2D:
            return new TypedValue.CastValue(Type.DOUBLE_TYPE, (TypedValue)value);
         case INEG:
            return new TypedValue.UnaryMathOpValue(TypedValue.UnaryMathOpValue.UnaryOp.neg, Type.INT_TYPE, (TypedValue)value);
         case LNEG:
            return new TypedValue.UnaryMathOpValue(TypedValue.UnaryMathOpValue.UnaryOp.neg, Type.LONG_TYPE, (TypedValue)value);
         case DNEG:
            return new TypedValue.UnaryMathOpValue(TypedValue.UnaryMathOpValue.UnaryOp.neg, Type.DOUBLE_TYPE, (TypedValue)value);
         case GETFIELD:  // this should normally fail, but a subclass can handle it
         case FNEG:
         case IINC:
         case I2B:
         case I2C:
         case I2S:
         case TABLESWITCH: 
         case LOOKUPSWITCH: 
         case PUTSTATIC:
         case NEWARRAY: 
         case ANEWARRAY: 
         case ARRAYLENGTH: 
         case ATHROW: 
         case INSTANCEOF:
         case MONITORENTER:
         case MONITOREXIT: 
         default:
            throw new AnalyzerException(insn, "Unhandled bytecode instruction");
      }
   }

   public Value binaryOperation(AbstractInsnNode insn, Value value1,
                                Value value2) throws AnalyzerException
   {
      switch (insn.getOpcode())
      {
         case IF_ICMPEQ: 
         case IF_ICMPNE: 
         case IF_ICMPLT: 
         case IF_ICMPGE: 
         case IF_ICMPGT: 
         case IF_ICMPLE:
         case IF_ACMPEQ: 
         case IF_ACMPNE:
            return ifOperation(insn, value1, value2);
         case ISUB:
         case LSUB: 
         case FSUB: 
         case DSUB: 
            return new TypedValue.MathOpValue(TypedValue.MathOpValue.Op.minus, (TypedValue)value1, (TypedValue)value2);
         case IADD:
         case LADD: 
         case FADD: 
         case DADD: 
            return new TypedValue.MathOpValue(TypedValue.MathOpValue.Op.plus, (TypedValue)value1, (TypedValue)value2);
         case IMUL:
         case LMUL: 
         case FMUL: 
         case DMUL: 
            return new TypedValue.MathOpValue(TypedValue.MathOpValue.Op.mul, (TypedValue)value1, (TypedValue)value2);
         case IDIV:
         case LDIV: 
         case FDIV: 
         case DDIV: 
            return new TypedValue.MathOpValue(TypedValue.MathOpValue.Op.div, (TypedValue)value1, (TypedValue)value2);
         case LCMP: 
         case FCMPL: 
         case FCMPG: 
         case DCMPL:
         case DCMPG: 
            return new TypedValue.MathOpValue(TypedValue.MathOpValue.Op.cmp, Type.INT_TYPE, (TypedValue)value1, (TypedValue)value2);
         case IREM: 
         case LREM: 
            return new TypedValue.MathOpValue(TypedValue.MathOpValue.Op.mod, Type.INT_TYPE, (TypedValue)value1, (TypedValue)value2);
         case IALOAD:
         case LALOAD: 
         case FALOAD: 
         case DALOAD: 
         case AALOAD: 
         case BALOAD: 
         case CALOAD: 
         case SALOAD: 
         case FREM: 
         case DREM: 
         case ISHL: 
         case LSHL: 
         case ISHR: 
         case LSHR: 
         case IUSHR:
         case LUSHR: 
         case IAND: 
         case LAND: 
         case IOR: 
         case LOR: 
         case IXOR: 
         case LXOR: 
         case PUTFIELD:
         default:
            throw new AnalyzerException(insn, "Unhandled bytecode instruction");
      }
   }

   public Value ternaryOperation(AbstractInsnNode insn, Value value1,
                                 Value value2, Value value3)
         throws AnalyzerException
   {
      switch(insn.getOpcode())
      {
         case IASTORE: 
         case LASTORE:
         case FASTORE: 
         case DASTORE:
         case AASTORE:
         case BASTORE:
         case CASTORE:
         case SASTORE:
         default:
            throw new AnalyzerException(insn, "Unhandled bytecode instruction");
      }
   }

   public Value naryOperation(AbstractInsnNode insn, List values)
         throws AnalyzerException
   {
      switch(insn.getOpcode())
      {
         case INVOKEVIRTUAL: 
         case INVOKESPECIAL: 
         case INVOKESTATIC: 
         case INVOKEINTERFACE:
         {
            // TODO: Check that methods have no side-effects
            
            assert(insn instanceof MethodInsnNode);
            MethodInsnNode methodInsn = (MethodInsnNode)insn;
            boolean isVirtualCall = (insn.getOpcode() != INVOKESTATIC);
            List<TypedValue> args = new ArrayList<TypedValue>();
            for (int n = (isVirtualCall? 1:0); n < values.size(); n++)
               args.add((TypedValue)values.get(n));
            MethodSignature sig = new MethodSignature(methodInsn.owner, methodInsn.name, methodInsn.desc);
            if (isVirtualCall)
            {
               TypedValue base = (TypedValue)values.get(0);
               if (methodChecker != null)
               {
                  OperationSideEffect sideEffect = methodChecker.isMethodSafe(sig, base, args); 
                  if (sideEffect == OperationSideEffect.UNSAFE)
                     throw new AnalyzerException(insn, "Unknown method " + sig + " encountered");
                  else if (sideEffect == OperationSideEffect.SAFE)
                     sideEffects.add(new MethodSideEffectCall(sig, base, args));
               }
               MethodCallValue.VirtualMethodCallValue toReturn;
               toReturn = new MethodCallValue.VirtualMethodCallValue(methodInsn.owner, methodInsn.name, methodInsn.desc, args, base);
               if (toReturn.isConstructor() && linkedFrame != null)
                  linkedFrame.replaceValues(base, toReturn);
               else if (methodChecker != null && methodChecker.isFluentChaining(sig))
                  linkedFrame.replaceValues(base, toReturn);
               return toReturn;
            }
            else
            {
               if (methodChecker != null)
               {
                  OperationSideEffect sideEffect = methodChecker.isStaticMethodSafe(sig); 
                  if (sideEffect == OperationSideEffect.UNSAFE)
                     throw new AnalyzerException(insn, "Unknown static method " + sig + " encountered");
                  else if (sideEffect == OperationSideEffect.SAFE)
                     sideEffects.add(new MethodSideEffectCall(sig, null, args));
               }
               return new MethodCallValue.StaticMethodCallValue(methodInsn.owner, methodInsn.name, methodInsn.desc, args);
            }
         }
         case INVOKEDYNAMIC:
         {
            assert(insn instanceof InvokeDynamicInsnNode);
            InvokeDynamicInsnNode invokeInsn = (InvokeDynamicInsnNode)insn;
            if (!"java/lang/invoke/LambdaMetafactory".equals(invokeInsn.bsm.getOwner()) 
                  || !"altMetafactory".equals(invokeInsn.bsm.getName())
                  || !"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;".equals(invokeInsn.bsm.getDesc()))
               throw new AnalyzerException(insn, "Unknown invokedynamic " + invokeInsn.bsm + " encountered");
            // Return the Lambda creation result
            Handle lambdaMethod = (Handle)invokeInsn.bsmArgs[1];
            Type functionalInterface = Type.getReturnType(invokeInsn.desc);
            return new LambdaFactory(functionalInterface, lambdaMethod, new ArrayList<>((List<TypedValue>)values));
         }
         case MULTIANEWARRAY:
         default:
            throw new AnalyzerException(insn, "Unhandled bytecode instruction");
      }
   }

   public void returnOperation(AbstractInsnNode insn, Value value,
                               Value expected) throws AnalyzerException
   {
      switch(insn.getOpcode())
      {
         case IRETURN: 
         case LRETURN: 
         case FRETURN: 
         case DRETURN: 
         case ARETURN:
            returnValue = (TypedValue)value;
            break;
         default:
      }
   }

   public Value ifOperation(AbstractInsnNode insn, Value value1, 
                            Value value2) throws AnalyzerException
   {
      TypedValue.ComparisonValue.ComparisonOp op;
      switch (insn.getOpcode())
      {
         case IFEQ: 
         case IF_ICMPEQ:
            op = TypedValue.ComparisonValue.ComparisonOp.eq;
            break;
         case IFNE:
         case IF_ICMPNE:
            op = TypedValue.ComparisonValue.ComparisonOp.ne;
            break;
         case IFLT: 
         case IF_ICMPLT:
            op = TypedValue.ComparisonValue.ComparisonOp.lt;
            break;
         case IFGE: 
         case IF_ICMPGE:
            op = TypedValue.ComparisonValue.ComparisonOp.ge;
            break;
         case IFGT: 
         case IF_ICMPGT:
            op = TypedValue.ComparisonValue.ComparisonOp.gt;
            break;
         case IFLE:
         case IF_ICMPLE:
            op = TypedValue.ComparisonValue.ComparisonOp.le;
            break;
         case IFNULL:
         case IF_ACMPEQ:
            op = TypedValue.ComparisonValue.ComparisonOp.eq;
            break;
         case IFNONNULL:
         case IF_ACMPNE:
            op = TypedValue.ComparisonValue.ComparisonOp.ne;
            break;
         default:
            throw new AnalyzerException(insn, "Unhandled bytecode instruction");
      }
      TypedValue.ComparisonValue toReturn =
         new TypedValue.ComparisonValue(op, (TypedValue)value1, (TypedValue)value2);
      
      if (branchHandler != null)
         branchHandler.ifInstruction(insn, toReturn);
      return toReturn;
   }
   


}
