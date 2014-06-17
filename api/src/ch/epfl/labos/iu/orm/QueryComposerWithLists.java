package ch.epfl.labos.iu.orm;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jinq.orm.stream.JinqStream;
import org.jinq.tuples.Pair;

import ch.epfl.labos.iu.orm.DBSet.AggregateDouble;
import ch.epfl.labos.iu.orm.DBSet.AggregateGroup;
import ch.epfl.labos.iu.orm.DBSet.AggregateInteger;
import ch.epfl.labos.iu.orm.DBSet.AggregateSelect;
import ch.epfl.labos.iu.orm.DBSet.Join;
import ch.epfl.labos.iu.orm.DBSet.Select;
import ch.epfl.labos.iu.orm.DBSet.Where;

public interface QueryComposerWithLists<T> extends QueryComposer<T>
{
   // Actually executes the query and returns the results in a set
   public VectorSet<T> createRealizedSet();

   // New stuff for Queryll2
   public QueryComposerWithLists<T> where(Where<T> test);
   public <U> QueryComposerWithLists<U> select(Select<T, U> select);
   public <U> QueryComposerWithLists<Pair<T, U>> join(Join<T,U> join);
   public <U, V> QueryComposerWithLists<Pair<U, V>> group(Select<T, U> select, AggregateGroup<U, T, V> aggregate);

   // returns null if the aggregates cannot be calculated
   public Double sumDouble(AggregateDouble<T> aggregate);
   public Integer sumInt(AggregateInteger<T> aggregate);
   public Double maxDouble(AggregateDouble<T> aggregate);
   public Integer maxInt(AggregateInteger<T> aggregate);
   public <U> U selectAggregates(AggregateSelect<T, U> aggregate);
}
