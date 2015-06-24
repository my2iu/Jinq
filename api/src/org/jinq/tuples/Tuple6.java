package org.jinq.tuples;

public class Tuple6<A, B, C, D, E, F> extends Tuple
{
   final A one;
   final B two;
   final C three;
   final D four;
   final E five;
   final F six;
   
   public A getOne() { return one; }
   public B getTwo() { return two; }
   public C getThree() { return three; }
   public D getFour() { return four; }
   public E getFive() { return five; }
   public F getSix() { return six; }
   
   public Tuple6(A one, B two, C three, D four, E five, F six)
   {
      this.one = one;
      this.two = two;
      this.three = three;
      this.four = four;
      this.five = five;
      this.six = six;
   }
   
   @Override
   public String toString()
   {
      return "Tuple6(" + getOne() + "," + getTwo() + "," + getThree() + "," + getFour() + "," + getFive() + "," + getSix() + ")";
   }

   public boolean equals(Object obj)
   {
      if ( this == obj) return true;
      
      if (! (obj instanceof Tuple6)) return false;
      
      Tuple6<?,?,?,?,?,?> tuple = (Tuple6<?,?,?,?,?,?>)obj;
      
      return (this.one == null ? tuple.one == null : this.one.equals(tuple.one))
            && (this.two == null ? tuple.two == null : this.two.equals(tuple.two))
            && (this.three == null ? tuple.three == null : this.three.equals(tuple.three))
            && (this.four == null ? tuple.four == null : this.four.equals(tuple.four))
            && (this.five == null ? tuple.five == null : this.five.equals(tuple.five))
            && (this.six == null ? tuple.six == null : this.six.equals(tuple.six));
   }
   
   public int hashCode()
   {
      return one.hashCode() + two.hashCode() + three.hashCode() + four.hashCode()
         + five.hashCode() + six.hashCode();
   }
   
}
