package ch.epfl.labos.iu.orm;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.jinq.tuples.Pair;

abstract public class BaseSet<T> implements DBSet<T>
{
   abstract public int size();
   abstract public Iterator<T> iterator();
   abstract public boolean add(T o);
   abstract public boolean remove(Object o);

   // Reusable stuff that depends on other api

   public T getValue()
   {
      assert(size() == 1);
      return iterator().next();
   }
   public T get()
   {
      return getValue();
   }

   public DBSet<T> with(T toAdd)
   {
      DBSet<T> newset = new VectorSet<T>();
      for (T o: this)
         newset.add(o);
      newset.add(toAdd);
      return newset;
   }
   
   public DBSet<T> sortedByIntAscending(final IntSorter<T> sorter)
   {
      Vector<T> sorted = new Vector<T>();
      sorted.addAll(this);
      Collections.sort(sorted, new Comparator<T>(){
         public int compare(T o1, T o2)
         {
            return (new Integer(sorter.value(o1))).compareTo(new Integer(sorter.value(o2)));
         }
      });
      VectorSet<T> newSet = new VectorSet<T>();
      newSet.addAll(sorted);
      return newSet;
   }
   public DBSet<T> sortedByIntDescending(final IntSorter<T> sorter)
   {
      Vector<T> sorted = new Vector<T>();
      sorted.addAll(this);
      Collections.sort(sorted, new Comparator<T>(){
         public int compare(T o1, T o2)
         {
            return -(new Integer(sorter.value(o1))).compareTo(new Integer(sorter.value(o2)));
         }
      });
      VectorSet<T> newSet = new VectorSet<T>();
      newSet.addAll(sorted);
      return newSet;
   }
   public DBSet<T> sortedByDoubleAscending(final DoubleSorter<T> sorter)
   {
      Vector<T> sorted = new Vector<T>();
      sorted.addAll(this);
      Collections.sort(sorted, new Comparator<T>(){
         public int compare(T o1, T o2)
         {
            return (new Double(sorter.value(o1))).compareTo(new Double(sorter.value(o2)));
         }
      });
      VectorSet<T> newSet = new VectorSet<T>();
      newSet.addAll(sorted);
      return newSet;
   }
   public DBSet<T> sortedByDoubleDescending(final DoubleSorter<T> sorter)
   {
      Vector<T> sorted = new Vector<T>();
      sorted.addAll(this);
      Collections.sort(sorted, new Comparator<T>(){
         public int compare(T o1, T o2)
         {
            return -(new Double(sorter.value(o1))).compareTo(new Double(sorter.value(o2)));
         }
      });
      VectorSet<T> newSet = new VectorSet<T>();
      newSet.addAll(sorted);
      return newSet;
   }
   public DBSet<T> sortedByStringAscending(final StringSorter<T> sorter)
   {
      Vector<T> sorted = new Vector<T>();
      sorted.addAll(this);
      Collections.sort(sorted, new Comparator<T>(){
         public int compare(T o1, T o2)
         {
            return sorter.value(o1).compareTo(sorter.value(o2));
         }
      });
      VectorSet<T> newSet = new VectorSet<T>();
      newSet.addAll(sorted);
      return newSet;
   }
   public DBSet<T> sortedByStringDescending(final StringSorter<T> sorter)
   {
      Vector<T> sorted = new Vector<T>();
      sorted.addAll(this);
      Collections.sort(sorted, new Comparator<T>(){
         public int compare(T o1, T o2)
         {
            return -sorter.value(o1).compareTo(sorter.value(o2));
         }
      });
      VectorSet<T> newSet = new VectorSet<T>();
      newSet.addAll(sorted);
      return newSet;
   }
   public DBSet<T> sortedByDateAscending(final DateSorter<T> sorter)
   {
      Vector<T> sorted = new Vector<T>();
      sorted.addAll(this);
      Collections.sort(sorted, new Comparator<T>(){
         public int compare(T o1, T o2)
         {
            return sorter.value(o1).compareTo(sorter.value(o2));
         }
      });
      VectorSet<T> newSet = new VectorSet<T>();
      newSet.addAll(sorted);
      return newSet;
   }
   public DBSet<T> sortedByDateDescending(final DateSorter<T> sorter)
   {
      Vector<T> sorted = new Vector<T>();
      sorted.addAll(this);
      Collections.sort(sorted, new Comparator<T>(){
         public int compare(T o1, T o2)
         {
            return -sorter.value(o1).compareTo(sorter.value(o2));
         }
      });
      VectorSet<T> newSet = new VectorSet<T>();
      newSet.addAll(sorted);
      return newSet;
   }
   public DBSet<T> firstN(int n)
   {
      VectorSet<T> newSet = new VectorSet<T>();
      for (T obj: this)
      {
         if (n <= 0) return newSet;
         n--;
         newSet.add(obj);
      }
      return newSet;
   }
   
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
   
   // New stuff for Queryll2
   public DBSet<T> where(Where<T> test)
   {
      DBSet<T> newset = new VectorSet<T>();
      for (T o: this)
         if (test.where(o))
            newset.add(o);
      return newset;
   }

   public <U> DBSet<U> select(Select<T, U> select)
   {
      DBSet<U> newset = new VectorSet<U>();
      for (T o: this)
         newset.add(select.select(o));
      return newset;
   }
   
   public <U> DBSet<Pair<T, U>> join(Join<T,U> join)
   {
      DBSet<Pair<T, U>> newset = new VectorSet<Pair<T, U>>();
      for (T o: this)
      {
         DBSet<U> other = join.join(o);
         for (U p: other)
            newset.add(new Pair<T, U>(o, p));
      }
      return newset;
   }
   
   public double sumDouble(AggregateDouble<T> aggregate)
   {
      double val = 0;
      for (T o: this)
         val += aggregate.aggregate(o);
      return val;
   }
   public int sumInt(AggregateInteger<T> aggregate)
   {
      int val = 0;
      for (T o: this)
         val += aggregate.aggregate(o);
      return val;
   }
   public double maxDouble(AggregateDouble<T> aggregate)
   {
      double val = Double.NEGATIVE_INFINITY;
      for (T o: this)
         val = Math.max(val, aggregate.aggregate(o));
      return val;
   }
   public int maxInt(AggregateInteger<T> aggregate)
   {
      int val = Integer.MIN_VALUE;
      for (T o: this)
         val = Math.max(val, aggregate.aggregate(o));
      return val;
   }
   public <U> U selectAggregates(AggregateSelect<T, U> aggregate)
   {
      U val = aggregate.aggregateSelect(this);
      return val;
   }
   
   public DBSet<T> unique()
   {
      DBSet<T> newset = new VectorSet<T>();
      for (T o: this)
         if (!newset.contains(o))
            newset.add(o);
      return newset;
   }

   public <U, V> DBSet<Pair<U, V>> group(Select<T, U> select, AggregateGroup<U, T, V> aggregate)
   {
      HashMap<U, DBSet<T>> map = new HashMap<U, DBSet<T>>();
      for (T o: this)
      {
         U key = select.select(o);
         if (!map.containsKey(key))
            map.put(key, new VectorSet<T>());
         map.get(key).add(o);
      }
      DBSet<Pair<U, V>> result = new VectorSet<Pair<U, V>>();
      for (Map.Entry<U, DBSet<T>> entry: map.entrySet())
      {
         result.add(new Pair<U, V>(entry.getKey(), aggregate.aggregateSelect(entry.getKey(), entry.getValue())));
      }
      return result;
   }
}
