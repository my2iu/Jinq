package org.jinq.orm.stream;

import java.io.Serializable;
import java.util.stream.Stream;

import ch.epfl.labos.iu.orm.DateSorter;
import ch.epfl.labos.iu.orm.DoubleSorter;
import ch.epfl.labos.iu.orm.IntSorter;
import ch.epfl.labos.iu.orm.Pair;
import ch.epfl.labos.iu.orm.StringSorter;
import ch.epfl.labos.iu.orm.DBSet.AggregateDouble;
import ch.epfl.labos.iu.orm.DBSet.AggregateGroup;
import ch.epfl.labos.iu.orm.DBSet.AggregateInteger;
import ch.epfl.labos.iu.orm.DBSet.AggregateSelect;
import ch.epfl.labos.iu.orm.DBSet.Join;
import ch.epfl.labos.iu.orm.DBSet.Select;

public interface JinqStream<T> extends Stream<T>
{
   public static interface Where<U> extends Serializable {
      public boolean where(U obj);
   }
   public JinqStream<T> where(Where<T> test);
   public <U> JinqStream<U> select(Select<T, U> select);
   // TODO: Rewrite join so that it doesn't take a DBSet
   public <U> JinqStream<Pair<T, U>> join(Join<T,U> join);
   public JinqStream<T> unique();
   public <U, V> JinqStream<Pair<U, V>> group(Select<T, U> select, AggregateGroup<U, T, V> aggregate);
   public double sumDouble(AggregateDouble<T> aggregate);
   public int sumInt(AggregateInteger<T> aggregate);
   public double maxDouble(AggregateDouble<T> aggregate);
   public int maxInt(AggregateInteger<T> aggregate);
   public <U> U selectAggregates(AggregateSelect<T, U> aggregate);

   public JinqStream<T> sortedByIntAscending(final IntSorter<T> sorter);
   public JinqStream<T> sortedByIntDescending(final IntSorter<T> sorter);
   public JinqStream<T> sortedByDoubleAscending(final DoubleSorter<T> sorter);
   public JinqStream<T> sortedByDoubleDescending(final DoubleSorter<T> sorter);
   public JinqStream<T> sortedByStringAscending(final StringSorter<T> sorter);
   public JinqStream<T> sortedByStringDescending(final StringSorter<T> sorter);
   public JinqStream<T> sortedByDateAscending(final DateSorter<T> sorter);
   public JinqStream<T> sortedByDateDescending(final DateSorter<T> sorter);
   
   public JinqStream<T> firstN(int n);
   
   public T getOnlyValue();
   public JinqStream<T> with(T toAdd);
}
