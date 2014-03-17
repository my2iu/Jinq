package ch.epfl.labos.iu.orm.queryll2.symbolic;


public interface SyntheticTypedValue
{
   // Certain types used in the symbolic execution are not based on real
   // Java types (useful for simplifying things etc), so we label them with 
   // this interface.
   
//   public static class BooleanConstant extends ConstantValue implements SyntheticTypedValue
//   {
//      public boolean val;
//      public BooleanConstant(boolean val)
//      {
//         super(Type.BOOLEAN_TYPE);
//         this.val = val;
//      }
//      public String toString() { return val ? "true" : "false"; }
//      @Override public <I,O> O visit(TypedValueVisitor<I,O> visitor, I input) throws TypedValueVisitorException
//      {
//         return visitor.booleanConstantValue(this, input);
//      }
//      @Override public Boolean getConstant() { return val; }
//      
//   }
}
