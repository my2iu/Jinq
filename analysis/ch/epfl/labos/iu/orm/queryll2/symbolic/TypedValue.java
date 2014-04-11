package ch.epfl.labos.iu.orm.queryll2.symbolic;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.Value;

// Holds symbolic values of variables. This is the base value that
// simply stores a type for the variable--it is specialized to hold
// constants and other symbolic values.

public class TypedValue implements Value
{
   Type type;
   public TypedValue(Type type)
   {
      this.type = type;
   }
   
   public Type getType()
   {
      return type;
   }
   
   public boolean isPrimitive()
   {
      if (type == null) return false;
      return !(type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY);
   }
   
   public int getSize()
   {
      if (type == null) return 1;
      return type.getSize();
   }
   
   public <I,O> O visit(TypedValueVisitor<I,O> visitor, I input) throws TypedValueVisitorException
   {
      return visitor.unknownValue(this, input);
   }
   

   // Various specializations of this TypedValue (I'm putting them as
   // inner classes so that I can group them together)
   public static class ThisValue extends TypedValue
   {
      public ThisValue(Type t)
      {
         super(t);
      }
      
      public String toString()
      {
         return "this";
      }
      
      @Override public <I,O> O visit(TypedValueVisitor<I,O> visitor, I input) throws TypedValueVisitorException
      {
         return visitor.thisValue(this, input);
      }
   }
   
   public static class ArgValue extends TypedValue
   {
      int index;
      public ArgValue(Type t, int index)
      {
         super(t);
         this.index = index;
      }
      public int getIndex() { return index; }
      public String toString()
      {
         return "@arg" + index;
      }
      @Override public <I,O> O visit(TypedValueVisitor<I,O> visitor, I input) throws TypedValueVisitorException
      {
         return visitor.argValue(this, input);
      }
   }

   public static class NewValue extends TypedValue
   {
      public NewValue(String classInternalName)
      {
         super(Type.getObjectType(classInternalName));
      }
      public String toString()
      {
         return "new " + type.getClassName();
      }
      @Override public <I,O> O visit(TypedValueVisitor<I,O> visitor, I input) throws TypedValueVisitorException
      {
         return visitor.newValue(this, input);
      }
   }

   public static class UnaryOperationValue extends TypedValue
   {
      public TypedValue operand;
      public UnaryOperationValue(Type resultType, TypedValue operand)
      {
         super(resultType);
         this.operand = operand;
      }
//      public String toString() 
//      { 
//         return "(" + left.toString() + " " + operation + " " + right.toString() + ")";
//      }
      @Override public <I,O> O visit(TypedValueVisitor<I,O> visitor, I input) throws TypedValueVisitorException
      {
         return visitor.unaryOpValue(this, input);
      }
      public UnaryOperationValue withNewChildren(TypedValue newOperand)
      {
         return new UnaryOperationValue(type, newOperand);
      }
   }
   public static class NotValue extends UnaryOperationValue
   {
      public NotValue(TypedValue operand)
      {
         super(Type.BOOLEAN_TYPE, operand);
      }
      public String toString() 
      { 
         return "(NOT " + operand.toString() + ")";
      }
      @Override public <I,O> O visit(TypedValueVisitor<I,O> visitor, I input) throws TypedValueVisitorException
      {
         return visitor.notOpValue(this, input);
      }
      public UnaryOperationValue withNewChildren(TypedValue newOperand)
      {
         return new NotValue(newOperand);
      }
   }
   public static class GetFieldValue extends UnaryOperationValue
   {
      public String owner;
      public String name;
      public String desc;
      public GetFieldValue(String owner, String name, String desc, TypedValue base)
      {
         super(Type.getType(desc), base);
         this.owner = owner;
         this.name = name;
         this.desc = desc;
      }
      public String toString()
      {
         return "(" + operand + "." + name + ")";
      }
      @Override public <I,O> O visit(TypedValueVisitor<I,O> visitor, I input) throws TypedValueVisitorException
      {
         return visitor.getFieldValue(this, input);
      }
      @Override public GetFieldValue withNewChildren(TypedValue newOperand)
      {
         return new GetFieldValue(owner, name, desc, newOperand);
      }
   }
   public static class CastValue extends UnaryOperationValue
   {
      public CastValue(Type castedType, TypedValue base)
      {
         super(castedType, base);
      }
      public String toString()
      {
         return "((" + type.getClassName() + ")" + operand + ")";
      }
      @Override public <I,O> O visit(TypedValueVisitor<I,O> visitor, I input) throws TypedValueVisitorException
      {
         return visitor.castValue(this, input);
      }
      @Override public CastValue withNewChildren(TypedValue newOperand)
      {
         return new CastValue(type, newOperand);
      }
   }
   
   public static class BinaryOperationValue extends TypedValue
   {
      public String operation;
      public TypedValue left;
      public TypedValue right;
      public BinaryOperationValue(Type resultType, String operation, TypedValue left, TypedValue right)
      {
         super(resultType);
         this.operation = operation;
         this.left = left;
         this.right = right;
      }
      public String toString() 
      { 
         return "(" + left.toString() + " " + operation + " " + right.toString() + ")";
      }
      @Override public <I,O> O visit(TypedValueVisitor<I,O> visitor, I input) throws TypedValueVisitorException
      {
         return visitor.binaryOpValue(this, input);
      }
      public BinaryOperationValue withNewChildren(TypedValue newLeft, TypedValue newRight)
      {
         return new BinaryOperationValue(type, operation, newLeft, newRight);
      }
   }
   public static class MathOpValue extends BinaryOperationValue
   {
      public enum Op
      {
         plus, minus, mul;
      }
      static String opToString(Op op)
      {
         switch(op)
         {
         case plus:  return "+";
         case minus:  return "-";
         case mul: return "*";
         default:  return "??";
         }
      }
      public String sqlOpString()
      {
         return opToString(op);
      }
      public Op op;
      public MathOpValue (Op op, TypedValue left, TypedValue right)
      {
         super(left.type, opToString(op), left, right);
         this.op = op;
      }
      @Override public <I,O> O visit(TypedValueVisitor<I,O> visitor, I input) throws TypedValueVisitorException
      {
         return visitor.mathOpValue(this, input);
      }
      @Override public MathOpValue withNewChildren(TypedValue newLeft, TypedValue newRight)
      {
         return new MathOpValue(op, newLeft, newRight);
      }
   }
   public static class ComparisonValue extends BinaryOperationValue
   {
      public enum ComparisonOp
      {
         eq, ne, lt, gt, le, ge;
      }
      static String compToString(ComparisonOp op)
      {
         switch(op)
         {
         case eq:  return "==";
         case ne:  return "!=";
         case lt:  return "<";
         case gt:  return ">";
         case le:  return "<=";
         default:
         case ge:  return ">=";
         }
      }
      public ComparisonOp compOp;
      public String sqlOpString()
      {
         switch(compOp)
         {
         case eq:  return "=";
         case ne:  return "<>";
         case lt:  return "<";
         case gt:  return ">";
         case le:  return "<=";
         default:
         case ge:  return ">=";
         }
      }
      public ComparisonValue(ComparisonOp operation, TypedValue left, TypedValue right)
      {
         super(Type.BOOLEAN_TYPE, compToString(operation), left, right);
         compOp = operation;
      }
      public ComparisonValue inverseValue()
      {
         ComparisonOp notOp;
         switch(compOp)
         {
            case eq:  notOp = ComparisonOp.ne; break;
            case ne:  notOp = ComparisonOp.eq; break;
            case lt:  notOp = ComparisonOp.ge; break;
            case gt:  notOp = ComparisonOp.le; break;
            case le:  notOp = ComparisonOp.gt; break;
            default:
            case ge:  notOp = ComparisonOp.lt; break;
         }
         return new ComparisonValue(notOp, left, right);
      }
      @Override public <I,O> O visit(TypedValueVisitor<I,O> visitor, I input) throws TypedValueVisitorException
      {
         return visitor.comparisonOpValue(this, input);
      }
      @Override public ComparisonValue withNewChildren(TypedValue newLeft, TypedValue newRight)
      {
         return new ComparisonValue(compOp, newLeft, newRight);
      }
   }
}
