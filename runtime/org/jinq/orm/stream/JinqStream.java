package org.jinq.orm.stream;

import java.util.stream.Stream;

import ch.epfl.labos.iu.orm.Pair;
import ch.epfl.labos.iu.orm.QueryComposer;
import ch.epfl.labos.iu.orm.DBSet.AggregateDouble;
import ch.epfl.labos.iu.orm.DBSet.AggregateGroup;
import ch.epfl.labos.iu.orm.DBSet.AggregateInteger;
import ch.epfl.labos.iu.orm.DBSet.AggregateSelect;
import ch.epfl.labos.iu.orm.DBSet.Join;
import ch.epfl.labos.iu.orm.DBSet.Select;
import ch.epfl.labos.iu.orm.DBSet.Where;

public interface JinqStream<T> extends Stream<T>
{
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
   
}
