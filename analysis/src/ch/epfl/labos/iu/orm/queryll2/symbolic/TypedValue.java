package ch.epfl.labos.iu.orm.queryll2.symbolic;

import org.jinq.rebased.org.objectweb.asm.Type;
import org.jinq.rebased.org.objectweb.asm.tree.analysis.Value;

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
   
   public <I,O,E extends Exception> O visit(TypedValueVisitor<I,O,E> visitor, I input) throws E
   {
      return visitor.unknownValue(this, input);
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((type == null) ? 0 : type.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      TypedValue other = (TypedValue) obj;
      if (type == null)
      {
         if (other.type != null)
            return false;
      } else if (!type.equals(other.type))
         return false;
      return true;
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
      
      @Override public <I,O, E extends Exception> O visit(TypedValueVisitor<I,O,E> visitor, I input) throws E
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
      @Override public <I,O,E extends Exception> O visit(TypedValueVisitor<I,O,E> visitor, I input) throws E
      {
         return visitor.argValue(this, input);
      }
      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = super.hashCode();
         result = prime * result + index;
         return result;
      }
      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (!super.equals(obj))
            return false;
         if (getClass() != obj.getClass())
            return false;
         ArgValue other = (ArgValue) obj;
         if (index != other.index)
            return false;
         return true;
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
      @Override public <I,O,E extends Exception> O visit(TypedValueVisitor<I,O,E> visitor, I input) throws E
      {
         return visitor.newValue(this, input);
      }
   }
   public static class GetStaticFieldValue extends TypedValue
   {
      public String owner;
      public String name;
      public String desc;
      public GetStaticFieldValue(String owner, String name, String desc)
      {
         super(Type.getType(desc));
         this.owner = owner;
         this.name = name;
         this.desc = desc;
      }
      public String toString()
      {
         return "(" + owner + "." + name + ")";
      }
      @Override public <I,O,E extends Exception> O visit(TypedValueVisitor<I,O,E> visitor, I input) throws E
      {
         return visitor.getStaticFieldValue(this, input);
      }
      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = super.hashCode();
         result = prime * result + ((desc == null) ? 0 : desc.hashCode());
         result = prime * result + ((name == null) ? 0 : name.hashCode());
         result = prime * result + ((owner == null) ? 0 : owner.hashCode());
         return result;
      }
      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (!super.equals(obj))
            return false;
         if (getClass() != obj.getClass())
            return false;
         GetStaticFieldValue other = (GetStaticFieldValue) obj;
         if (desc == null)
         {
            if (other.desc != null)
               return false;
         } else if (!desc.equals(other.desc))
            return false;
         if (name == null)
         {
            if (other.name != null)
               return false;
         } else if (!name.equals(other.name))
            return false;
         if (owner == null)
         {
            if (other.owner != null)
               return false;
         } else if (!owner.equals(other.owner))
            return false;
         return true;
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
//         return "(" + op.getOpString() + " " + operand.toString() + ")";
//      }
      @Override public <I,O,E extends Exception> O visit(TypedValueVisitor<I,O,E> visitor, I input) throws E
      {
         return visitor.unaryOpValue(this, input);
      }
      public UnaryOperationValue withNewChildren(TypedValue newOperand)
      {
         return new UnaryOperationValue(type, newOperand);
      }
      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = super.hashCode();
         result = prime * result + ((operand == null) ? 0 : operand.hashCode());
         return result;
      }
      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (!super.equals(obj))
            return false;
         if (getClass() != obj.getClass())
            return false;
         UnaryOperationValue other = (UnaryOperationValue) obj;
         if (operand == null)
         {
            if (other.operand != null)
               return false;
         } else if (!operand.equals(other.operand))
            return false;
         return true;
      }
      
   }
   public static class UnaryMathOpValue extends UnaryOperationValue
   {
      public UnaryOp op;
      public enum UnaryOp
      {
         neg("-");
         private UnaryOp(String str)
         {
            this.str = str;
         }
         public String getOpString() { return str; }
         String str;
      }
      public UnaryMathOpValue(UnaryOp op, Type resultType, TypedValue operand)
      {
         super(resultType, operand);
         this.op = op;
      }
      public String toString() 
      { 
         return "(" + op.getOpString() + " " + operand.toString() + ")";
      }
      @Override public <I,O,E extends Exception> O visit(TypedValueVisitor<I,O,E> visitor, I input) throws E
      {
         return visitor.unaryMathOpValue(this, input);
      }
      public UnaryOperationValue withNewChildren(TypedValue newOperand)
      {
         return new UnaryMathOpValue(op, type, newOperand);
      }
      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = super.hashCode();
         result = prime * result + ((op == null) ? 0 : op.hashCode());
         return result;
      }
      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (!super.equals(obj))
            return false;
         if (getClass() != obj.getClass())
            return false;
         UnaryMathOpValue other = (UnaryMathOpValue) obj;
         if (op != other.op)
            return false;
         return true;
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
      @Override public <I,O,E extends Exception> O visit(TypedValueVisitor<I,O,E> visitor, I input) throws E
      {
         return visitor.notOpValue(this, input);
      }
      public UnaryOperationValue withNewChildren(TypedValue newOperand)
      {
         return new NotValue(newOperand);
      }
      // Creates a value for NOT(val), but might simplify the 
      // resulting expression if it can do so easily
      public static TypedValue invert(TypedValue val)
      {
         if (val.getClass() == NotValue.class)
            return ((NotValue)val).operand;
         if (val instanceof ComparisonValue)
            return ((ComparisonValue)val).inverseValue();
         return new NotValue(val);
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
      @Override public <I,O,E extends Exception> O visit(TypedValueVisitor<I,O,E> visitor, I input) throws E
      {
         return visitor.getFieldValue(this, input);
      }
      @Override public GetFieldValue withNewChildren(TypedValue newOperand)
      {
         return new GetFieldValue(owner, name, desc, newOperand);
      }
      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = super.hashCode();
         result = prime * result + ((desc == null) ? 0 : desc.hashCode());
         result = prime * result + ((name == null) ? 0 : name.hashCode());
         result = prime * result + ((owner == null) ? 0 : owner.hashCode());
         return result;
      }
      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (!super.equals(obj))
            return false;
         if (getClass() != obj.getClass())
            return false;
         GetFieldValue other = (GetFieldValue) obj;
         if (desc == null)
         {
            if (other.desc != null)
               return false;
         } else if (!desc.equals(other.desc))
            return false;
         if (name == null)
         {
            if (other.name != null)
               return false;
         } else if (!name.equals(other.name))
            return false;
         if (owner == null)
         {
            if (other.owner != null)
               return false;
         } else if (!owner.equals(other.owner))
            return false;
         return true;
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
      @Override public <I,O,E extends Exception> O visit(TypedValueVisitor<I,O,E> visitor, I input) throws E
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
      @Override public <I,O,E extends Exception> O visit(TypedValueVisitor<I,O,E> visitor, I input) throws E
      {
         return visitor.binaryOpValue(this, input);
      }
      public BinaryOperationValue withNewChildren(TypedValue newLeft, TypedValue newRight)
      {
         return new BinaryOperationValue(type, operation, newLeft, newRight);
      }
      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = super.hashCode();
         result = prime * result + ((left == null) ? 0 : left.hashCode());
         result = prime * result
               + ((operation == null) ? 0 : operation.hashCode());
         result = prime * result + ((right == null) ? 0 : right.hashCode());
         return result;
      }
      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (!super.equals(obj))
            return false;
         if (getClass() != obj.getClass())
            return false;
         BinaryOperationValue other = (BinaryOperationValue) obj;
         if (left == null)
         {
            if (other.left != null)
               return false;
         } else if (!left.equals(other.left))
            return false;
         if (operation == null)
         {
            if (other.operation != null)
               return false;
         } else if (!operation.equals(other.operation))
            return false;
         if (right == null)
         {
            if (other.right != null)
               return false;
         } else if (!right.equals(other.right))
            return false;
         return true;
      }
   }
   public static class MathOpValue extends BinaryOperationValue
   {
      public enum Op
      {
         plus ("+"), minus("-"), mul("*"), div("/"), mod("%"), cmp("cmp");
         
         private Op(String opString)
         {
            this.opString = opString;
         }
         String opString;
         public String getOpString()
         {
            return opString;
         }
      }
      static String opToString(Op op)
      {
         return op.getOpString();
      }
      public String sqlOpString()
      {
         return opToString(op);
      }
      public Op op;
      public MathOpValue (Op op, Type returnType, TypedValue left, TypedValue right)
      {
         super(returnType, opToString(op), left, right);
         this.op = op;
      }
      public MathOpValue (Op op, TypedValue left, TypedValue right)
      {
         this(op, left.type, left, right);
      }
      @Override public <I,O,E extends Exception> O visit(TypedValueVisitor<I,O,E> visitor, I input) throws E
      {
         return visitor.mathOpValue(this, input);
      }
      @Override public MathOpValue withNewChildren(TypedValue newLeft, TypedValue newRight)
      {
         return new MathOpValue(op, newLeft, newRight);
      }
      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = super.hashCode();
         result = prime * result + ((op == null) ? 0 : op.hashCode());
         return result;
      }
      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (!super.equals(obj))
            return false;
         if (getClass() != obj.getClass())
            return false;
         MathOpValue other = (MathOpValue) obj;
         if (op != other.op)
            return false;
         return true;
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
      @Override public <I,O,E extends Exception> O visit(TypedValueVisitor<I,O,E> visitor, I input) throws E
      {
         return visitor.comparisonOpValue(this, input);
      }
      @Override public ComparisonValue withNewChildren(TypedValue newLeft, TypedValue newRight)
      {
         return new ComparisonValue(compOp, newLeft, newRight);
      }
      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = super.hashCode();
         result = prime * result + ((compOp == null) ? 0 : compOp.hashCode());
         return result;
      }
      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (!super.equals(obj))
            return false;
         if (getClass() != obj.getClass())
            return false;
         ComparisonValue other = (ComparisonValue) obj;
         if (compOp != other.compOp)
            return false;
         return true;
      }
   }
}
