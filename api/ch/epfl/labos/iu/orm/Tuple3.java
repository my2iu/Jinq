package ch.epfl.labos.iu.orm;

public class Tuple3<A, B, C>
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
   
   public boolean equals(Object obj)
   {
      if ( this == obj) return true;
      
      if (! (obj instanceof Tuple3)) return false;
      
      Tuple3 tuple = (Tuple3)obj;
      
      return this.one.equals(tuple.one)
         && this.two.equals(tuple.two)
         && this.three.equals(tuple.three);
   }
   
   public int hashCode()
   {
      return one.hashCode() + two.hashCode() + three.hashCode();
   }
   
}
