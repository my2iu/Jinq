package ch.epfl.labos.iu.orm;

public class Expand<From, To>
{
   public DBSet<To> expand(From obj)
   {
      return new VectorSet<To>();
   }
}
