package ch.epfl.labos.iu.orm;


public class Triple<T, U, V>
{
   final T one;
   final U two;
   final V three;
   
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
   
   public Triple(T one, U two, V three)
   {
      this.one = one;
      this.two = two;
      this.three = three;
   }
   
   public boolean equals(Object obj)
   {
      if ( this == obj) return true;
      
      if (! (obj instanceof Triple)) return false;
      
      return this.one.equals(((Triple)obj).one)
         && this.two.equals(((Triple)obj).two)
         && this.three.equals(((Triple)obj).three);
   }
   
   public int hashCode()
   {
      return one.hashCode() + two.hashCode() + three.hashCode();
   }
}
