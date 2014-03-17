package ch.epfl.labos.iu.orm;


public class Quartic<T, U, V, W>
{
   final T one;
   final U two;
   final V three;
   final W four;
   
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
   
   public Quartic(T one, U two, V three, W four)
   {
      this.one = one;
      this.two = two;
      this.three = three;
      this.four = four;
   }
   
   public boolean equals(Object obj)
   {
      if ( this == obj) return true;
      
      if (! (obj instanceof Quartic)) return false;
      
      Quartic<?,?,?,?> quartic = (Quartic<?,?,?,?>)obj;
      return this.one.equals(quartic.one)
         && this.two.equals(quartic.two)
         && this.three.equals(quartic.three)
         && this.four.equals(quartic.four);
   }
   
   public int hashCode()
   {
      return one.hashCode() + two.hashCode() + three.hashCode() + four.hashCode();
   }
}
