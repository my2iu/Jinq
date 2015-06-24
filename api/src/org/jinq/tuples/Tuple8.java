package org.jinq.tuples;

public class Tuple8<A, B, C, D, E, F, G, H> extends Tuple
{
   final A one;
   final B two;
   final C three;
   final D four;
   final E five;
   final F six;
   final G seven;
   final H eight;
   
   public A getOne() { return one; }
   public B getTwo() { return two; }
   public C getThree() { return three; }
   public D getFour() { return four; }
   public E getFive() { return five; }
   public F getSix() { return six; }
   public G getSeven() { return seven; }
   public H getEight() { return eight; }
   
   public Tuple8(A one, B two, C three, D four, E five, F six, G seven, H eight)
   {
      this.one = one;
      this.two = two;
      this.three = three;
      this.four = four;
      this.five = five;
      this.six = six;
      this.seven = seven;
      this.eight = eight;
   }
   
   @Override
   public String toString()
   {
      return "Tuple8(" + getOne() + "," + getTwo() + "," + getThree() + "," + getFour() + "," + getFive() + "," + getSix() + "," + getSeven() + "," + getEight() + ")";
   }

   public boolean equals(Object obj)
   {
      if ( this == obj) return true;
      
      if (! (obj instanceof Tuple8)) return false;
      
      Tuple8<?,?,?,?,?,?,?,?> tuple = (Tuple8<?,?,?,?,?,?,?,?>)obj;
      
      return (this.one == null ? tuple.one == null : this.one.equals(tuple.one))
            && (this.two == null ? tuple.two == null : this.two.equals(tuple.two))
            && (this.three == null ? tuple.three == null : this.three.equals(tuple.three))
            && (this.four == null ? tuple.four == null : this.four.equals(tuple.four))
            && (this.five == null ? tuple.five == null : this.five.equals(tuple.five))
            && (this.six == null ? tuple.six == null : this.six.equals(tuple.six))
            && (this.seven == null ? tuple.seven == null : this.seven.equals(tuple.seven))
            && (this.eight == null ? tuple.eight == null : this.eight.equals(tuple.eight));
   }
   
   public int hashCode()
   {
      return one.hashCode() + two.hashCode() + three.hashCode() + four.hashCode()
         + five.hashCode() + six.hashCode() + seven.hashCode() + eight.hashCode();
   }
   
}
