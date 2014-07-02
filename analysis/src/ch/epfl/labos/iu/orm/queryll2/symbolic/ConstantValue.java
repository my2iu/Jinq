/**
 * 
 */
package ch.epfl.labos.iu.orm.queryll2.symbolic;

import org.objectweb.asm.Type;

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
   }
}