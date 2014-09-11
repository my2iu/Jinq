package ch.epfl.labos.iu.orm;

import org.jinq.tuples.Pair;

public class QueryList<T> extends LazySet<T>
{
   public QueryList() {}
   public QueryList(QueryComposerWithLists<T> composer)
   {
      queryComposer = composer;
   }
   
   QueryComposerWithLists<T> queryComposer = null;

   public void setQueryComposer(QueryComposerWithLists<T> queryComposer)
   {
      this.queryComposer = queryComposer;
   }
   
//   Realizer<T, T> realizer;
//   
//   public void setRealizer(Realizer<T, T> realizer)
//   {
//      this.realizer = realizer;
//   }
//   
//   public Realizer<T, T> getRealizer()
//   {
//      return realizer;
//   }
   
   public boolean isRealized()
   {
      return !(data == null);
   }
   
   public Object clone() throws CloneNotSupportedException
   {
      QueryList<T> copy = (QueryList<T>)super.clone();
      copy.queryComposer = queryComposer;
      return copy;
   }
   
   protected VectorSet<T> createRealizedSet()
   {
      if (queryComposer != null)
         return queryComposer.createRealizedSet();
      else
         return new VectorSet<T>();
   }

//   public DBSet<T> sortedByIntAscending(final IntSorter<T> sorter)
//   {
//      if (isRealized() || !(realizer instanceof QueryListRealizer))
//         return super.sortedByIntAscending(sorter);
//      QueryListRealizer<T> qrealizer = (QueryListRealizer<T>)realizer;
//      qrealizer = new QueryListRealizer<T>(qrealizer.con, qrealizer.query.copy(), qrealizer.reader.copy());
//      if (qrealizer.query.isLimited())
//         return super.sortedByIntAscending(sorter);
//      if (!sorter.backdoor(qrealizer.query, qrealizer.reader, true))
//         return super.sortedByIntAscending(sorter);
//      QueryList<T> newSet = new QueryList<T>();
//      newSet.setRealizer(qrealizer);
//      return newSet;
//   }
//   public DBSet<T> sortedByIntDescending(final IntSorter<T> sorter)
//   {
//      if (isRealized() || !(realizer instanceof QueryListRealizer))
//         return super.sortedByIntDescending(sorter);
//      QueryListRealizer<T> qrealizer = (QueryListRealizer<T>)realizer;
//      qrealizer = new QueryListRealizer<T>(qrealizer.con, qrealizer.query.copy(), qrealizer.reader.copy());
//      if (qrealizer.query.isLimited())
//         return super.sortedByIntDescending(sorter);
//      if (!sorter.backdoor(qrealizer.query, qrealizer.reader, false))
//         return super.sortedByIntAscending(sorter);
//      QueryList<T> newSet = new QueryList<T>();
//      newSet.setRealizer(qrealizer);
//      return newSet;
//   }
//   public DBSet<T> sortedByDoubleAscending(final DoubleSorter<T> sorter)
//   {
//      if (isRealized() || !(realizer instanceof QueryListRealizer))
//         return super.sortedByDoubleAscending(sorter);
//      QueryListRealizer<T> qrealizer = (QueryListRealizer<T>)realizer;
//      qrealizer = new QueryListRealizer<T>(qrealizer.con, qrealizer.query.copy(), qrealizer.reader.copy());
//      if (qrealizer.query.isLimited())
//         return super.sortedByDoubleAscending(sorter);
//      if (!sorter.backdoor(qrealizer.query, qrealizer.reader, true))
//         return super.sortedByDoubleAscending(sorter);
//      QueryList<T> newSet = new QueryList<T>();
//      newSet.setRealizer(qrealizer);
//      return newSet;
//   }
//   public DBSet<T> sortedByDoubleDescending(final DoubleSorter<T> sorter)
//   {
//      if (isRealized() || !(realizer instanceof QueryListRealizer))
//         return super.sortedByDoubleDescending(sorter);
//      QueryListRealizer<T> qrealizer = (QueryListRealizer<T>)realizer;
//      qrealizer = new QueryListRealizer<T>(qrealizer.con, qrealizer.query.copy(), qrealizer.reader.copy());
//      if (qrealizer.query.isLimited())
//         return super.sortedByDoubleDescending(sorter);
//      if (!sorter.backdoor(qrealizer.query, qrealizer.reader, false))
//         return super.sortedByDoubleAscending(sorter);
//      QueryList<T> newSet = new QueryList<T>();
//      newSet.setRealizer(qrealizer);
//      return newSet;
//   }
//   public DBSet<T> sortedByStringAscending(final StringSorter<T> sorter)
//   {
//      if (isRealized() || !(realizer instanceof QueryListRealizer))
//         return super.sortedByStringAscending(sorter);
//      QueryListRealizer<T> qrealizer = (QueryListRealizer<T>)realizer;
//      qrealizer = new QueryListRealizer<T>(qrealizer.con, qrealizer.query.copy(), qrealizer.reader.copy());
//      if (qrealizer.query.isLimited())
//         return super.sortedByStringAscending(sorter);
//      if (!sorter.backdoor(qrealizer.query, qrealizer.reader, true))
//         return super.sortedByStringAscending(sorter);
//      QueryList<T> newSet = new QueryList<T>();
//      newSet.setRealizer(qrealizer);
//      return newSet;
//   }
//   public DBSet<T> sortedByStringDescending(final StringSorter<T> sorter)
//   {
//      if (isRealized() || !(realizer instanceof QueryListRealizer))
//         return super.sortedByStringDescending(sorter);
//      QueryListRealizer<T> qrealizer = (QueryListRealizer<T>)realizer;
//      qrealizer = new QueryListRealizer<T>(qrealizer.con, qrealizer.query.copy(), qrealizer.reader.copy());
//      if (qrealizer.query.isLimited())
//         return super.sortedByStringDescending(sorter);
//      if (!sorter.backdoor(qrealizer.query, qrealizer.reader, false))
//         return super.sortedByStringDescending(sorter);
//      QueryList<T> newSet = new QueryList<T>();
//      newSet.setRealizer(qrealizer);
//      return newSet;
//   }
//   public DBSet<T> sortedByDateAscending(final DateSorter<T> sorter)
//   {
//      if (isRealized() || !(realizer instanceof QueryListRealizer))
//         return super.sortedByDateAscending(sorter);
//      QueryListRealizer<T> qrealizer = (QueryListRealizer<T>)realizer;
//      qrealizer = new QueryListRealizer<T>(qrealizer.con, qrealizer.query.copy(), qrealizer.reader.copy());
//      if (qrealizer.query.isLimited())
//         return super.sortedByDateAscending(sorter);
//      if (!sorter.backdoor(qrealizer.query, qrealizer.reader, true))
//         return super.sortedByDateAscending(sorter);
//      QueryList<T> newSet = new QueryList<T>();
//      newSet.setRealizer(qrealizer);
//      return newSet;
//   }
//   public DBSet<T> sortedByDateDescending(final DateSorter<T> sorter)
//   {
//      if (isRealized() || !(realizer instanceof QueryListRealizer))
//         return super.sortedByDateDescending(sorter);
//      QueryListRealizer<T> qrealizer = (QueryListRealizer<T>)realizer;
//      qrealizer = new QueryListRealizer<T>(qrealizer.con, qrealizer.query.copy(), qrealizer.reader.copy());
//      if (qrealizer.query.isLimited())
//         return super.sortedByDateDescending(sorter);
//      if (!sorter.backdoor(qrealizer.query, qrealizer.reader, false))
//         return super.sortedByDateDescending(sorter);
//      QueryList<T> newSet = new QueryList<T>();
//      newSet.setRealizer(qrealizer);
//      return newSet;
//   }
//   public DBSet<T> firstN(int n)
//   {
//      if (isRealized() || !(realizer instanceof QueryListRealizer))
//         return super.firstN(n);
//      QueryListRealizer<T> qrealizer = (QueryListRealizer<T>)realizer;
//      qrealizer = new QueryListRealizer<T>(qrealizer.con, qrealizer.query.copy(), qrealizer.reader.copy());
//      qrealizer.query.limit(n);
//      QueryList<T> newSet = new QueryList<T>();
//      newSet.setRealizer(qrealizer);
//      return newSet;
//   }

   public void bulkAdd(T o)
   {
      add(o);
   }

   public DBSet<T> with(T toAdd)
   {
      // TODO: This is properly supported yet (need a way to have an 
      // empty QueryList with a QueryComposer inside it, but right
      // now the QueryComposer is null because we don't know what 
      // type to put there)
      if (queryComposer != null && !isRealized())
      {
         QueryComposerWithLists<T> newComposer = (QueryComposerWithLists<T>)queryComposer.with(toAdd);
         if (newComposer != null) return new QueryList<T>(newComposer);
      }
      return super.with(toAdd);
   }

   public DBSet<T> sortedByIntAscending(final IntSorter<T> sorter)
   {
      if (queryComposer != null && !isRealized())
      {
         QueryComposerWithLists<T> newComposer = (QueryComposerWithLists<T>)queryComposer.sortedByInt(sorter, true);
         if (newComposer != null) return new QueryList<T>(newComposer);
      }
      return super.sortedByIntAscending(sorter);
   }
   public DBSet<T> sortedByIntDescending(final IntSorter<T> sorter)
   {
      if (queryComposer != null && !isRealized())
      {
         QueryComposerWithLists<T> newComposer = (QueryComposerWithLists<T>)queryComposer.sortedByInt(sorter, false);
         if (newComposer != null) return new QueryList<T>(newComposer);
      }
      return super.sortedByIntAscending(sorter);
   }
   public DBSet<T> sortedByDoubleAscending(final DoubleSorter<T> sorter)
   {
      if (queryComposer != null && !isRealized())
      {
         QueryComposerWithLists<T> newComposer = (QueryComposerWithLists<T>)queryComposer.sortedByDouble(sorter, true);
         if (newComposer != null) return new QueryList<T>(newComposer);
      }
      return super.sortedByDoubleAscending(sorter);
   }
   public DBSet<T> sortedByDoubleDescending(final DoubleSorter<T> sorter)
   {
      if (queryComposer != null && !isRealized())
      {
         QueryComposerWithLists<T> newComposer = (QueryComposerWithLists<T>)queryComposer.sortedByDouble(sorter, false);
         if (newComposer != null) return new QueryList<T>(newComposer);
      }
      return super.sortedByDoubleDescending(sorter);
   }
   public DBSet<T> sortedByStringAscending(final StringSorter<T> sorter)
   {
      if (queryComposer != null && !isRealized())
      {
         QueryComposerWithLists<T> newComposer = (QueryComposerWithLists<T>)queryComposer.sortedByString(sorter, true);
         if (newComposer != null) return new QueryList<T>(newComposer);
      }
      return super.sortedByStringAscending(sorter);
   }
   public DBSet<T> sortedByStringDescending(final StringSorter<T> sorter)
   {
      if (queryComposer != null && !isRealized())
      {
         QueryComposerWithLists<T> newComposer = (QueryComposerWithLists<T>)queryComposer.sortedByString(sorter, false);
         if (newComposer != null) return new QueryList<T>(newComposer);
      }
      return super.sortedByStringDescending(sorter);
   }
   public DBSet<T> sortedByDateAscending(final DateSorter<T> sorter)
   {
      if (queryComposer != null && !isRealized())
      {
         QueryComposerWithLists<T> newComposer = (QueryComposerWithLists<T>)queryComposer.sortedByDate(sorter, true);
         if (newComposer != null) return new QueryList<T>(newComposer);
      }
      return super.sortedByDateAscending(sorter);
   }
   public DBSet<T> sortedByDateDescending(final DateSorter<T> sorter)
   {
      if (queryComposer != null && !isRealized())
      {
         QueryComposerWithLists<T> newComposer = (QueryComposerWithLists<T>)queryComposer.sortedByDate(sorter, false);
         if (newComposer != null) return new QueryList<T>(newComposer);
      }
      return super.sortedByDateDescending(sorter);
   }
   
   public DBSet<T> firstN(int n)
   {
      if (queryComposer != null && !isRealized())
      {
         QueryComposerWithLists<T> newComposer = (QueryComposerWithLists<T>)queryComposer.limit(n);
         if (newComposer != null) return new QueryList<T>(newComposer);
      }
      return super.firstN(n);
   }
   
   public DBSet<T> where(Where<T> test)
   {
      if (queryComposer != null && !isRealized())
      {
         QueryComposerWithLists<T> newComposer = queryComposer.where(test);
         if (newComposer != null) return new QueryList<T>(newComposer);
      }
      return super.where(test);
   }
   public <U> DBSet<U> select(Select<T, U> select)
   {
      if (queryComposer != null && !isRealized())
      {
         QueryComposerWithLists<U> newComposer = queryComposer.select(select);
         if (newComposer != null) return new QueryList<U>(newComposer);
      }
      return super.select(select);
   }
   public <U> DBSet<Pair<T, U>> join(Join<T,U> join)
   {
      if (queryComposer != null && !isRealized())
      {
         QueryComposerWithLists<Pair<T, U>> newComposer = queryComposer.join(join);
         if (newComposer != null) return new QueryList<Pair<T, U>>(newComposer);
      }
      return super.join(join);
   }
   public DBSet<T> unique()
   {
      if (queryComposer != null && !isRealized())
      {
         QueryComposerWithLists<T> newComposer = (QueryComposerWithLists<T>)queryComposer.distinct();
         if (newComposer != null) return new QueryList<T>(newComposer);
      }
      return super.unique();
   }
   public <U, V> DBSet<Pair<U, V>> group(Select<T, U> select, AggregateGroup<U, T, V> aggregate)
   {
      if (queryComposer != null && !isRealized())
      {
         QueryComposerWithLists<Pair<U, V>> newComposer = queryComposer.group(select, aggregate);
         if (newComposer != null) return new QueryList<Pair<U, V>>(newComposer);
      }
      return super.group(select, aggregate);
   }

   public double sumDouble(AggregateDouble<T> aggregate)
   {
      if (queryComposer != null && !isRealized())
      {
         Double val = queryComposer.sumDouble(aggregate);
         if (val != null) return val;
      }
      return super.sumDouble(aggregate);
   }
   public int sumInt(AggregateInteger<T> aggregate)
   {
      if (queryComposer != null && !isRealized())
      {
         Integer val = queryComposer.sumInt(aggregate);
         if (val != null) return val;
      }
      return super.sumInt(aggregate);
   }
   public double maxDouble(AggregateDouble<T> aggregate)
   {
      if (queryComposer != null && !isRealized())
      {
         Double val = queryComposer.maxDouble(aggregate);
         if (val != null) return val;
      }
      return super.maxDouble(aggregate);
   }
   public int maxInt(AggregateInteger<T> aggregate)
   {
      if (queryComposer != null && !isRealized())
      {
         Integer val = queryComposer.maxInt(aggregate);
         if (val != null) return val;
      }
      return super.maxInt(aggregate);
   }
   public <U> U selectAggregates(AggregateSelect<T, U> aggregate)
   {
      if (queryComposer != null && !isRealized())
      {
         U val = queryComposer.selectAggregates(aggregate);
         if (val != null) return val;
      }
      return super.selectAggregates(aggregate);
   }

}
