/**
 * 
 */
package ch.epfl.labos.iu.orm.queryll2.symbolic;

import org.jinq.rebased.org.objectweb.asm.Type;

public class ConstantValue extends TypedValue
{
   public ConstantValue(Type t)
   {
      super(t);
   }
   @Override public <I,O,E extends Exception> O visit(TypedValueVisitor<I,O,E> visitor, I input) throws E
   {
      return visitor.constantValue(this, input);
   }
   public Object getConstant() { return null; }

   /**
    * Boolean constants don't appear naturally in the code. They are inferred
    * by the SymbExBooleanRewriter
    */
   public static class BooleanConstant extends ConstantValue
   {
      public boolean val;
      public BooleanConstant(boolean val)
      {
         super(Type.BOOLEAN_TYPE);
         this.val = val;
      }
      public String toString() { return Boolean.toString(val); }
      @Override public <I,O,E extends Exception> O visit(TypedValueVisitor<I,O,E> visitor, I input) throws E
      {
         return visitor.booleanConstantValue(this, input);
      }
      @Override public Boolean getConstant() { return val; }
      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = super.hashCode();
         result = prime * result + (val ? 1231 : 1237);
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
         BooleanConstant other = (BooleanConstant) obj;
         if (val != other.val)
            return false;
         return true;
      }
   }
   public static class ByteConstant extends ConstantValue
   {
      public byte val;
      public ByteConstant(byte val)
      {
         super(Type.BYTE_TYPE);
         this.val = val;
      }
      public String toString() { return Byte.toString(val); }
      @Override public <I,O,E extends Exception> O visit(TypedValueVisitor<I,O,E> visitor, I input) throws E
      {
         return visitor.byteConstantValue(this, input);
      }
      @Override public Byte getConstant() { return val; }
      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = super.hashCode();
         result = prime * result + val;
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
         ByteConstant other = (ByteConstant) obj;
         if (val != other.val)
            return false;
         return true;
      }
   }
   public static class ShortConstant extends ConstantValue
   {
      public short val;
      public ShortConstant(short val)
      {
         super(Type.SHORT_TYPE);
         this.val = val;
      }
      public String toString() { return Short.toString(val); }
      @Override public <I,O,E extends Exception> O visit(TypedValueVisitor<I,O,E> visitor, I input) throws E
      {
         return visitor.shortConstantValue(this, input);
      }
      @Override public Short getConstant() { return val; }
      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = super.hashCode();
         result = prime * result + val;
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
         ShortConstant other = (ShortConstant) obj;
         if (val != other.val)
            return false;
         return true;
      }
   }
   public static class IntegerConstant extends ConstantValue
   {
      public int val;
      public IntegerConstant(int val)
      {
         super(Type.INT_TYPE);
         this.val = val;
      }
      public String toString() { return Integer.toString(val); }
      @Override public <I,O,E extends Exception> O visit(TypedValueVisitor<I,O,E> visitor, I input) throws E
      {
         return visitor.integerConstantValue(this, input);
      }
      @Override public Integer getConstant() { return val; }
      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = super.hashCode();
         result = prime * result + val;
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
         IntegerConstant other = (IntegerConstant) obj;
         if (val != other.val)
            return false;
         return true;
      }
   }
   public static class LongConstant extends ConstantValue
   {
      public long val;
      public LongConstant(long val)
      {
         super(Type.LONG_TYPE);
         this.val = val;
      }
      public String toString() { return Long.toString(val); }
      @Override public <I,O,E extends Exception> O visit(TypedValueVisitor<I,O,E> visitor, I input) throws E
      {
         return visitor.longConstantValue(this, input);
      }
      @Override public Long getConstant() { return val; }
      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = super.hashCode();
         result = prime * result + (int) (val ^ (val >>> 32));
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
         LongConstant other = (LongConstant) obj;
         if (val != other.val)
            return false;
         return true;
      }
   }
   public static class FloatConstant extends ConstantValue
   {
      public float val;
      public FloatConstant(float val)
      {
         super(Type.FLOAT_TYPE);
         this.val = val;
      }
      public String toString() { return Float.toString(val); }
      @Override public <I,O,E extends Exception> O visit(TypedValueVisitor<I,O,E> visitor, I input) throws E
      {
         return visitor.floatConstantValue(this, input);
      }
      @Override public Float getConstant() { return val; }
      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = super.hashCode();
         result = prime * result + Float.floatToIntBits(val);
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
         FloatConstant other = (FloatConstant) obj;
         if (Float.floatToIntBits(val) != Float.floatToIntBits(other.val))
            return false;
         return true;
      }
   }
   public static class DoubleConstant extends ConstantValue
   {
      public double val;
      public DoubleConstant(double val)
      {
         super(Type.DOUBLE_TYPE);
         this.val = val;
      }
      public String toString() { return Double.toString(val); }
      @Override public <I,O,E extends Exception> O visit(TypedValueVisitor<I,O,E> visitor, I input) throws E
      {
         return visitor.doubleConstantValue(this, input);
      }
      @Override public Double getConstant() { return val; }
      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = super.hashCode();
         long temp;
         temp = Double.doubleToLongBits(val);
         result = prime * result + (int) (temp ^ (temp >>> 32));
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
         DoubleConstant other = (DoubleConstant) obj;
         if (Double.doubleToLongBits(val) != Double.doubleToLongBits(other.val))
            return false;
         return true;
      }
   }
   public static class NullConstant extends ConstantValue
   {
      public NullConstant()
      {
         super(Type.getObjectType("null"));
      }
      public String toString() { return "null"; }
      @Override public <I,O,E extends Exception> O visit(TypedValueVisitor<I,O,E> visitor, I input) throws E
      {
         return visitor.nullConstantValue(this, input);
      }
      @Override public Object getConstant() { return null; }
   }
   public static class StringConstant extends ConstantValue
   {
      public String val;
      public StringConstant(String val)
      {
         
         super(Type.getObjectType("java/lang/String"));
         this.val = val;
      }
      public String toString() { return "\"" + val + "\""; }
      @Override public <I,O,E extends Exception> O visit(TypedValueVisitor<I,O,E> visitor, I input) throws E
      {
         return visitor.stringConstantValue(this, input);
      }
      @Override public String getConstant() { return val; }
      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = super.hashCode();
         result = prime * result + ((val == null) ? 0 : val.hashCode());
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
         StringConstant other = (StringConstant) obj;
         if (val == null)
         {
            if (other.val != null)
               return false;
         } else if (!val.equals(other.val))
            return false;
         return true;
      }
   }
   public static class ClassConstant extends ConstantValue
   {
      public Type val;
      public ClassConstant(Type val)
      {
         super(Type.getObjectType("java/lang/Class"));
         this.val = val;
      }
      public String toString() { return val.getClassName() + ".class"; }
      @Override public <I,O,E extends Exception> O visit(TypedValueVisitor<I,O,E> visitor, I input) throws E
      {
         return visitor.classConstantValue(this, input);
      }
      @Override public Type getConstant() { return val; }
      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = super.hashCode();
         result = prime * result + ((val == null) ? 0 : val.hashCode());
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
         ClassConstant other = (ClassConstant) obj;
         if (val == null)
         {
            if (other.val != null)
               return false;
         } else if (!val.equals(other.val))
            return false;
         return true;
      }
   }
}