package org.jinq.tuples;

public class Tuple3<A, B, C> extends Tuple
{
   final A one;
   final B two;
   final C three;
   
   public A getOne() { return one; }
   public B getTwo() { return two; }
   public C getThree() { return three; }
   
   public Tuple3(A one, B two, C three)
   {
      this.one = one;
      this.two = two;
      this.three = three;
   }
   
   @Override
   public String toString()
   {
      return "Tuple3(" + getOne() + "," + getTwo() + "," + getThree() + ")";
   }
   
   public boolean equals(Object obj)
   {
      if ( this == obj) return true;
      
      if (! (obj instanceof Tuple3)) return false;
      
      Tuple3<?,?,?> tuple = (Tuple3<?,?,?>)obj;

      return (this.one == null ? tuple.one == null : this.one.equals(tuple.one))
            && (this.two == null ? tuple.two == null : this.two.equals(tuple.two))
            && (this.three == null ? tuple.three == null : this.three.equals(tuple.three));
   }
   
   public int hashCode()
   {
      return one.hashCode() + two.hashCode() + three.hashCode();
   }
   
}
