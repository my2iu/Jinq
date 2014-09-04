package org.jinq.tuples;

public class Tuple4<A, B, C, D> extends Tuple
{
   final A one;
   final B two;
   final C three;
   final D four;
   
   public A getOne() { return one; }
   public B getTwo() { return two; }
   public C getThree() { return three; }
   public D getFour() { return four; }
   
   public Tuple4(A one, B two, C three, D four)
   {
      this.one = one;
      this.two = two;
      this.three = three;
      this.four = four;
   }
   
   @Override
   public String toString()
   {
      return "Tuple4(" + getOne() + "," + getTwo() + "," + getThree() + "," + getFour() + ")";
   }

   public boolean equals(Object obj)
   {
      if ( this == obj) return true;
      
      if (! (obj instanceof Tuple4)) return false;
      
      Tuple4<?,?,?,?> tuple = (Tuple4<?,?,?,?>)obj;
      
      return (this.one == null ? tuple.one == null : this.one.equals(tuple.one))
            && (this.two == null ? tuple.two == null : this.two.equals(tuple.two))
            && (this.three == null ? tuple.three == null : this.three.equals(tuple.three))
            && (this.four == null ? tuple.four == null : this.four.equals(tuple.four));
   }
   
   public int hashCode()
   {
      return one.hashCode() + two.hashCode() + three.hashCode() + four.hashCode();
   }
   
}
