package ch.epfl.labos.iu.orm;

import java.sql.Date;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class QueryMap<K, V> implements Map<K, V>
{
   Vector<K> keys = new Vector<K>();
   Vector<V> values = new Vector<V>();

   V defaultValue;
   
   public QueryMap(V defaultValue)
   {
      this.defaultValue = defaultValue;
   }
   
   protected QueryMap(Vector<K> keys, Vector<V> values, V defaultValue)
   {
      this.keys = keys;
      this.values = values;
      this.defaultValue = defaultValue;
   }
   
   public void clear()
   {
      keys.clear();
      values.clear();
   }

   public boolean containsKey(Object key)
   {
      return keys.contains(key);
   }

   public boolean containsValue(Object value)
   {
      return values.contains(value);
   }

   public V get(Object key)
   {
      int idx = keys.indexOf(key);
      if (idx == -1) return defaultValue;
      return values.get(idx);
   }

   public boolean isEmpty()
   {
      return values.isEmpty();
   }

   public V put(K key, V value)
   {
      int idx = keys.indexOf(key);
      V oldValue;
      if (idx == -1)
      {
         oldValue = defaultValue;
         keys.add(key);
         values.add(value);
      }
      else
      {
         oldValue = values.get(idx);
         values.set(idx, value);
      }
      return oldValue;
   }

   public void bulkPut(K key, V value)
   {
      keys.add(key);
      values.add(value);
   }
   
   public void putAll(Map<? extends K, ? extends V> t)
   {
      for (Entry<? extends K, ? extends V> e: t.entrySet())
      {
         put(e.getKey(), e.getValue());
      }
   }

   public V remove(Object key)
   {
      int idx = keys.indexOf(key);
      V oldValue;
      if (idx == -1)
      {
         oldValue = defaultValue;
      }
      else
      {
         oldValue = values.get(idx);
         keys.remove(idx);
         values.remove(idx);
      }
      return oldValue;
   }

   public int size()
   {
      return values.size();
   }

   // TODO: Add support for removal of elements from the set
   public Set<K> keySet()
   {
      Set<K> set = new HashSet<K>();
      set.addAll(keys);
      return set;
   }

   // TODO: Add proper support for removal of elements from the set
   public Collection<V> values()
   {
      return values;
   }
   
   protected class EntrySetEntry<K, V> implements java.util.Map.Entry<K, V>
   {
      K key;
      V val;
      EntrySetEntry(K key, V val)
      {
         this.key = key;
         this.val = val;
      }
      public K getKey()
      {
         return key;
      }

      public V getValue()
      {
         return val;
      }

      public V setValue(V value)
      {
         V oldVal = val;
         val = value;
         return oldVal;
      }
   }
   
   // TODO: Add support for removal of elements from the set
   public Set<java.util.Map.Entry<K, V>> entrySet()
   {
      Set<java.util.Map.Entry<K, V>> set = new HashSet<java.util.Map.Entry<K, V>>();
      for (int n = 0; n < keys.size(); n++)
      {
         set.add(new EntrySetEntry<K, V>(keys.get(n), values.get(n)));
      }
         
      return set;
   }

   public DBSet<Pair<K, V>> asList()
   {
      DBSet<Pair<K, V>> set = new VectorSet<Pair<K, V>>();
      for (int n = 0; n < keys.size(); n++)
      {
         set.add(new Pair<K, V>(keys.get(n), values.get(n)));
      }
         
      return set;
   }

//   public DBGrouper<K, V> firstN(int num)
//   {
//      Vector<K> newKeys = new Vector<K>();
//      Vector<V> newVals = new Vector<V>();
//      
//      if (num > keys.size()) num = keys.size();
//      for (int i = 0; i < num; i++)
//      {
//         newKeys.add(keys.get(i));
//         newVals.add(values.get(i));
//      }
//      return new QueryMap<K, V>(newKeys, newVals, defaultValue);
//   }
//
//   protected Pair<K, V>[] toSortArray()
//   {
//      Pair<K, V>[] arr = new Pair[keys.size()];
//      for (int n = 0; n < keys.size(); n++)
//      {
//         arr[n] = new Pair<K, V>(keys.get(n), values.get(n));
//      }
//      return arr;
//   }
//   
//   protected DBGrouper<K, V> sortedWithComparator(Comparator<Pair<? extends K, ? extends V>> comparator)
//   {
//      Pair<K, V>[] arr = toSortArray();
//      Arrays.sort(arr, comparator);
//
//      Vector<K> newKeys = new Vector<K>();
//      Vector<V> newVals = new Vector<V>();
//      
//      for (int i = 0; i < arr.length; i++)
//      {
//         newKeys.add(arr[i].getOne());
//         newVals.add(arr[i].getTwo());
//      }
//      return new QueryMap<K, V>(newKeys, newVals, defaultValue);
//   }
//   
//   public DBGrouper<K, V> sortedByDateAscending(final DateMapSorter<K, V> sorter)
//   {
//      return sortedWithComparator(new Comparator<Pair<? extends K, ? extends V>>()
//            {
//               public int compare(Pair<? extends K, ? extends V> o1,
//                     Pair<? extends K, ? extends V> o2)
//               {
//                  Date a = sorter.value(o1.getOne(), o1.getTwo());
//                  Date b = sorter.value(o2.getOne(), o2.getTwo());
//                  return a.compareTo(b);
//               }}
//         );
//   }
//
//   public DBGrouper<K, V> sortedByDateDescending(final DateMapSorter<K, V> sorter)
//   {
//      return sortedWithComparator(new Comparator<Pair<? extends K, ? extends V>>()
//            {
//               public int compare(Pair<? extends K, ? extends V> o1,
//                     Pair<? extends K, ? extends V> o2)
//               {
//                  Date a = sorter.value(o1.getOne(), o1.getTwo());
//                  Date b = sorter.value(o2.getOne(), o2.getTwo());
//                  return -a.compareTo(b);
//               }}
//         );
//   }
//
//   public DBGrouper<K, V> sortedByIntAscending(final IntMapSorter<K, V> sorter)
//   {
//      return sortedWithComparator(new Comparator<Pair<? extends K, ? extends V>>()
//            {
//               public int compare(Pair<? extends K, ? extends V> o1,
//                     Pair<? extends K, ? extends V> o2)
//               {
//                  int a = sorter.value(o1.getOne(), o1.getTwo());
//                  int b = sorter.value(o2.getOne(), o2.getTwo());
//                  if (a < b) return -1;
//                  if (b < a) return 1;
//                  return 0;
//               }}
//         );
//   }
//
//   public DBGrouper<K, V> sortedByIntDescending(final IntMapSorter<K, V> sorter)
//   {
//      return sortedWithComparator(new Comparator<Pair<? extends K, ? extends V>>()
//            {
//               public int compare(Pair<? extends K, ? extends V> o1,
//                     Pair<? extends K, ? extends V> o2)
//               {
//                  int a = sorter.value(o1.getOne(), o1.getTwo());
//                  int b = sorter.value(o2.getOne(), o2.getTwo());
//                  if (a < b) return 1;
//                  if (b < a) return -1;
//                  return 0;
//               }}
//         );
//   }
//
//   public DBGrouper<K, V> sortedByStringAscending(final StringMapSorter<K, V> sorter)
//   {
//      return sortedWithComparator(new Comparator<Pair<? extends K, ? extends V>>()
//            {
//               public int compare(Pair<? extends K, ? extends V> o1,
//                     Pair<? extends K, ? extends V> o2)
//               {
//                  String a = sorter.value(o1.getOne(), o1.getTwo());
//                  String b = sorter.value(o2.getOne(), o2.getTwo());
//                  return a.compareTo(b);
//               }}
//         );
//   }
//
//   public DBGrouper<K, V> sortedByStringDescending(final StringMapSorter<K, V> sorter)
//   {
//      return sortedWithComparator(new Comparator<Pair<? extends K, ? extends V>>()
//            {
//               public int compare(Pair<? extends K, ? extends V> o1,
//                     Pair<? extends K, ? extends V> o2)
//               {
//                  String a = sorter.value(o1.getOne(), o1.getTwo());
//                  String b = sorter.value(o2.getOne(), o2.getTwo());
//                  return -a.compareTo(b);
//               }}
//         );
//   }
//
}
