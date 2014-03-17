package ch.epfl.labos.iu.orm;


public class Quintic<T, U, V, W, X>
{
   final T one;
   final U two;
   final V three;
   final W four;
   final X five;
   
   public T getOne()
   {
      return one;
   }
   
   public U getTwo()
   {
      return two;
   }

   public V getThree()
   {
      return three;
   }

   public W getFour()
   {
      return four;
   }

   public X getFive()
   {
      return five;
   }

   public Quintic(T one, U two, V three, W four, X five)
   {
      this.one = one;
      this.two = two;
      this.three = three;
      this.four = four;
      this.five = five;
   }
   
   public boolean equals(Object obj)
   {
      if ( this == obj) return true;
      
      if (! (obj instanceof Quintic)) return false;
      
      Quintic<?,?,?,?,?> quintic = (Quintic<?,?,?,?,?>)obj;
      return this.one.equals(quintic.one)
         && this.two.equals(quintic.two)
         && this.three.equals(quintic.three)
         && this.four.equals(quintic.four)
         && this.five.equals(quintic.five);
   }
   
   public int hashCode()
   {
      return one.hashCode() + two.hashCode() + three.hashCode() + four.hashCode() + five.hashCode();
   }
}
