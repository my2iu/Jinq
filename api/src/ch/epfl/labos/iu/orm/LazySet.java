package ch.epfl.labos.iu.orm;

import java.util.Iterator;

public class LazySet<T> extends BaseSet<T>
{
   LazySet<T> listener;
   protected VectorSet<T> data;

   public DBSet<T> comparisonClone()
   {
      if (data != null)
         return data.comparisonClone();
      LazySet<T> copy = new LazySet<T>();
      return copy;
   }
   
   
   public Object clone() throws CloneNotSupportedException
   {
      LazySet<T> copy = (LazySet<T>)super.clone();
      if (data != null)
         copy.data = (VectorSet<T>)data.clone();
      return copy;
   }
   
   public LazySet()
   {
      data = null;
   }
   
   public LazySet(boolean isEmpty)
   {
      if (isEmpty)
         data = new VectorSet<T>();
      else
         data = null;
   }
   
   public void setRealizeListener(LazySet<T> listener)
   {
      this.listener = listener;
   }
   
   public void realize()
   {
      if (data == null)
      {
         data = createRealizedSet();
         if (listener != null)
         {
            try {
               listener.data = (VectorSet<T>)data.clone();
            } catch (CloneNotSupportedException e)
            {
               e.printStackTrace();
            }
         }
      }
   }
   
   protected VectorSet<T> createRealizedSet()
   {
      return null;
   }

   public int size()
   {
      realize();
      return data.size();
   }

   public Iterator<T> iterator()
   {
      Iterator<T> lazyIterator = new LazyIterator<T>(this);
      return lazyIterator;
   }

   public boolean add(T o)
   {
      realize();
      return data.add(o);
   }

   public boolean remove(Object o)
   {
      realize();
      return data.remove(o);
   }
}
