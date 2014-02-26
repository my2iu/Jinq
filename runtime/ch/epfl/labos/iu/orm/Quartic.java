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
      
      return this.one.equals(((Quartic)obj).one)
         && this.two.equals(((Quartic)obj).two)
         && this.three.equals(((Quartic)obj).three)
         && this.four.equals(((Quartic)obj).four);
   }
   
   public int hashCode()
   {
      return one.hashCode() + two.hashCode() + three.hashCode() + four.hashCode();
   }
}
