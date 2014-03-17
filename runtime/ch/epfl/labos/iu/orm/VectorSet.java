package ch.epfl.labos.iu.orm;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

public class VectorSet<T> extends BaseSet<T> implements Cloneable
{
   public DBSet<T> comparisonClone()
   {
      VectorSet<T> copy = new VectorSet<T>();
      copy.data = (Collection<T>)((Vector<T>)data).clone();
      return copy;
   }
   
   public Object clone() throws CloneNotSupportedException
   {
      VectorSet<T> copy = (VectorSet<T>)super.clone();
      copy.data = (Collection<T>)((Vector<T>)data).clone();
      return copy;
   }
   
   Collection<T> data = new Vector<T>();
   
   public int size()
   {
      return data.size();
   }
   
   public Iterator<T> iterator()
   {
      return data.iterator();
   }

   public boolean add(T o)
   {
      return data.add(o);
   }
   
   public boolean remove(Object o)
   {
      return data.remove(o);
   }

   
   
   // Reusable stuff that depends on other api

   public boolean isEmpty()
   {
      return size() == 0;
   }

   public boolean contains(Object o)
   {
      for (T obj: this)
         if (obj.equals(o))
            return true;
      return false;
   }

   public Object[] toArray()
   {
      Object [] arr = new Object[size()];
      int n = 0;
      for (T obj: this)
      {
         arr[n] = obj;
         n++;
      }
      return arr;
   }

   public <U> U[] toArray(U[] a)
   {
      U [] arr;
      if (a.length >= size())
         arr = a;
      else
         arr = (U[])java.lang.reflect.Array.newInstance(
            a.getClass().getComponentType(), size());
      int n = 0;
      for (T obj: this)
      {
         arr[n] = (U)obj;
         n++;
      }
      return arr;
   }

   public boolean containsAll(Collection<?> c)
   {
      for (Object o: c)
      {
         if (!contains(o))
            return false;
      }
      return true;
   }

   public boolean addAll(Collection<? extends T> c)
   {
      boolean changed = false;
      for (T obj: c)
         changed |= add(obj);
      return changed;
   }

   public boolean removeAll(Collection<?> c)
   {
      boolean changed = false;
      for (Object obj: c)
         changed |= remove(obj);
      return changed;
   }

   public boolean retainAll(Collection<?> c)
   {
      Vector<T> toDelete = new Vector<T>();
      for (T obj: this)
         if (!c.contains(obj))
            toDelete.add(obj);
      return removeAll(toDelete);
   }

   public void clear()
   {
      Vector<T> toDelete = new Vector<T>();
      for (T obj: this)
         toDelete.add(obj);
      removeAll(toDelete);
   }
}
