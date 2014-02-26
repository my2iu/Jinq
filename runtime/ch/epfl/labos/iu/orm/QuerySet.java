package ch.epfl.labos.iu.orm;

import java.util.Collection;
import java.util.Iterator;

public class QuerySet<T> extends LazySet<T>
{
   Realizer<T, T> realizer;
   
   public void setRealizer(Realizer<T, T> realizer)
   {
      this.realizer = realizer;
   }
   
   public Realizer<T, T> getRealizer()
   {
      return realizer;
   }
   
   public boolean isRealized()
   {
      return !(data == null);
   }

   protected VectorSet<T> createRealizedSet()
   {
      if (realizer != null)
         return realizer.createRealizedSet();
      else
         return new VectorSet<T>();
   }


   
//   public QuerySet()
//   {
//      data = new VectorSet<T>();
//   }
//   
//   @Override
//   public int size()
//   {
//      return data.size();
//   }

//   @Override
//   public Iterator<T> iterator()
//   {
//      return data.iterator();
//   }

   @Override
   public boolean add(T o)
   {
      realize();
      if (data.contains(o)) return false;
      return data.add(o);
   }

//   @Override
//   public boolean remove(Object o)
//   {
//      return data.remove(o);
//   }

//   public DBSet<T> comparisonClone()
//   {
//      QuerySet<T> copy = new QuerySet<T>();
//      copy.data = (VectorSet<T>) data.comparisonClone();
//      return copy;
//   }
   
   public Object clone() throws CloneNotSupportedException
   {
      QuerySet<T> copy = (QuerySet<T>)super.clone();
      copy.realizer = realizer;
//      copy.data = (VectorSet<T>)data.clone();
      return copy;
   }
   
   public void bulkAdd(T o)
   {
      add(o);
   }
}
