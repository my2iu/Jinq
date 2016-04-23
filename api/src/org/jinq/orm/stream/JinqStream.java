package org.jinq.orm.stream;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jinq.tuples.Pair;
import org.jinq.tuples.Tuple3;
import org.jinq.tuples.Tuple4;
import org.jinq.tuples.Tuple5;

/**
 * The JinqStream is a normal Java 8 stream extended with extra methods. These
 * extra methods are needed because:
 * <ul>
 * <li>They can be more easily analyzed by Jinq
 * <li>They provide extra functionality that more closely mirrors database query
 * functionality
 * <li>They have names that are derived from database query syntax instead of
 * names derived from functional programming syntax
 * </ul>
 * <p>
 * Programmers typically get access to <code>JinqStream</code>s through a
 * JinqStream provider. For example, the
 * {@code org.jinq.jpa.JinqJPAStreamProvider} is able to create streams of JPA
 * entities from a database. It is also possible to create
 * <code>JinqStream</code>s from collections and single objects.
 * 
 * @param <T>
 *           type of object that is being processed by the stream
 */
public interface JinqStream<T> extends Stream<T>
{
   @FunctionalInterface
   public static interface Where<U, E extends Exception> extends Serializable
   {
      public boolean where(U obj) throws E;
   }

   @FunctionalInterface
   public static interface WhereWithSource<U, E extends Exception> extends
         Serializable
   {
      public boolean where(U obj, InQueryStreamSource source) throws E;
   }

   /**
    * Filters the elements of the stream.
    * 
    * <pre>
    * {@code JinqStream<Customer> stream = ...;
    * JinqStream<Customer> result = stream.where(c -> c.getName().equals("Alice"));
    * }
    * </pre>
    * 
    * @param test
    *           function applied to the elements of the stream. When passed an
    *           element from the stream, the function should return true if the
    *           element should be kept. if the function returns false, the
    *           element is discarded.
    * @return a new stream that returns only the elements satisfying the filter
    */
   public <E extends Exception> JinqStream<T> where(Where<T, E> test);

   /**
    * Filters the elements of the stream. This version allows the filter
    * function to take a second parameter with an InQueryStreamSource. This lets
    * the function create new streams of elements that it can use in subqueries.
    * 
    * @see #where(Where)
    * @param test
    *           function applied to each element of the stream. The function is
    *           passed an element from the stream as well as an
    *           {@link InQueryStreamSource}. The function should return true if
    *           the element should be kept. if the function returns false, the
    *           element is discarded.
    * @return a new stream that returns only the elements satisfying the filter
    */
   public <E extends Exception> JinqStream<T> where(WhereWithSource<T, E> test);

   @FunctionalInterface
   public static interface Select<U, V> extends Serializable
   {
      public V select(U val);
   }

   @FunctionalInterface
   public static interface SelectWithSource<U, V> extends Serializable
   {
      public V select(U val, InQueryStreamSource source);
   }

   /**
    * Transforms the elements in the stream. The method allows you to rewrite
    * each element from the stream, so that they contain only certain fields or
    * to do some calculation based on the values of the fields.
    * 
    * <pre>
    * {@code JinqStream<Customer> stream = ...;
    * JinqStream<String> result = stream.select(c -> c.getName());
    * }
    * </pre>
    * 
    * @param select
    *           function applied to the elements of the stream. When passed an
    *           element from the stream, the function should return a new value
    *           that should be used instead of the element in the stream.
    * @return a new stream that uses only the new rewritten stream elements
    */
   public <U> JinqStream<U> select(Select<T, U> select);

   /**
    * Transforms the elements in the stream. This version also passes an
    * {@link InQueryStreamSource} to the select function so that the function
    * can create new streams of elements to use in subqueries.
    * 
    * @see #select(Select)
    */
   public <U> JinqStream<U> select(SelectWithSource<T, U> select);

   /**
    * Transforms the elements in the stream. The method allows you to rewrite
    * each element from the stream, so that they contain only certain fields or
    * to do some calculation based on the values of the fields. Unlike a normal
    * select(), this method allows you to return a stream of elements. The 
    * stream elements will all be added to the final stream.
    * 
    * <pre>
    * {@code JinqStream<Country> stream = ...;
    * JinqStream<City> result = stream.selectAll(c -> JinqStream.from(c.getCities()));
    * }
    * </pre>
    * 
    * @see #select(Select)
    * @param select
    *           function applied to the elements of the stream. When passed an
    *           element from the stream, the function should return a stream of
    *           new values that will be flattened and placed in the new stream
    * @return a new stream that uses only the new rewritten stream elements
    */
   public <U> JinqStream<U> selectAll(Join<T, U> select);

   /**
    * Transforms the elements in the stream. This version also passes an
    * {@link InQueryStreamSource} to the select function so that the function
    * can create new streams of elements to use in subqueries.
    * 
    * @see #selectAll(Join)
    */
   public <U> JinqStream<U> selectAll(JoinWithSource<T, U> select);

   /**
    * A variant of selectAll() that can be used if you want to join to a
    * collection without the trouble of converting it to a JinqStream
    * first.
    * 
    * <pre>
    * {@code JinqStream<Country> stream = ...;
    * JinqStream<City> result = stream.selectAll(c -> c.getCities());
    * }
    * </pre>
    * 
    * @see #selectAll(Join)
    * @param select
    *           function applied to the elements of the stream. When passed an
    *           element from the stream, the function should return a collection 
    *           of new values that will be flattened and placed in the new stream
    * @return a new stream that uses only the new rewritten stream elements
    */
   public <U> JinqStream<U> selectAllList(JoinToIterable<T, U> select);
   
   // TODO: Joins are somewhat dangerous because certain types of joins that are
   // expressible here are NOT expressible in SQL. (Moving a join into
   // a from clause is only possible if the join does not access variables from
   // other things in the FROM clause *if* it ends up as a subquery. If we can
   // express it as not a subquery, then it's ok.
   // TODO: Perhaps only providing a join(DBSet<U> other) is safer because
   // I think it will translate into valid SQL code, but it prevents people from
   // using navigational queries e.g. customers.join(customer ->
   // customer.getPurchases);
   @FunctionalInterface
   public static interface Join<U, V> extends Serializable
   {
      public JinqStream<V> join(U val);
   }

   @FunctionalInterface
   public static interface JoinWithSource<U, V> extends Serializable
   {
      public JinqStream<V> join(U val, InQueryStreamSource source);
   }

   @FunctionalInterface
   public static interface JoinToIterable<U, V> extends Serializable
   {
      public Iterable<V> join(U val);
   }

   /**
    * Pairs up each entry of the stream with a stream of related elements.
    * 
    * <pre>
    * {@code JinqStream<Country> stream = ...;
    * JinqStream<Pair<Country, City>> result = 
    *    stream.join(c -> JinqStream.from(c.getCities()));
    * }
    * </pre>
    * 
    * @param join
    *           function applied to the elements of the stream. When passed an
    *           element from the stream, the function should return a stream of
    *           values that should be paired up with that stream element.
    * @return a new stream with the paired up elements
    */
   public <U> JinqStream<Pair<T, U>> join(Join<T, U> join);

   /**
    * Pairs up each entry of the stream with a stream of related elements. This
    * version also passes an {@link InQueryStreamSource} to the join function so
    * that the function can join elements with unrelated streams of entities
    * from a database.
    * 
    * @see #join(Join)
    */
   public <U> JinqStream<Pair<T, U>> join(JoinWithSource<T, U> join);

   /**
    * A variant of join() that can be used if you want to join to a
    * collection without the trouble of converting it to a JinqStream
    * first.
    * 
    * @see #join(Join)
    */
   public <U> JinqStream<Pair<T, U>> joinList(JoinToIterable<T, U> join);

   /**
    * Pairs up each entry of the stream with a stream of related elements. Uses
    * a left outer join during the pairing up process, so even if an element is
    * not joined with anything, a pair will still be created in the output
    * stream consisting of the element paired with null.
    * 
    * <pre>
    * {@code JinqStream<Country> stream = ...;
    * JinqStream<Pair<Country, Mountain>> result = 
    *    stream.leftOuterJoin(c -> JinqStream.from(c.getMountain()));
    * JinqStream<Pair<Country, Mountain>> result = 
    *    stream.leftOuterJoin(c -> JinqStream.of(c.getHighestMountain()));
    * }
    * </pre>
    * 
    * @see #join(Join)
    * @param join
    *           function applied to the elements of the stream. When passed an
    *           element from the stream, the function should return a stream of
    *           values that should be paired up with that stream element. The
    *           function must use a JPA association or navigational link as the
    *           base for the stream returned. Both singular or plural
    *           associations are allowed.
    * @return a new stream with the paired up elements
    */
   public <U> JinqStream<Pair<T, U>> leftOuterJoin(Join<T, U> join);

   /**
    * A variant of leftOuterJoin() that can be used if you want to join to a
    * collection without the trouble of converting it to a JinqStream
    * first.
    * 
    * @see #leftOuterJoin(Join)
    */
   public <U> JinqStream<Pair<T, U>> leftOuterJoinList(JoinToIterable<T, U> join);

   @FunctionalInterface
   public static interface WhereForOn<U, V> extends Serializable
   {
      public boolean where(U obj1, V obj2);
   }
   
   /**
    * Pairs up each entry of the stream with a stream of related elements. Uses
    * a left outer join during the pairing up process, so even if an element is
    * not joined with anything, a pair will still be created in the output
    * stream consisting of the element paired with null. This version also passes 
    * an {@link InQueryStreamSource} to the join function so that the function 
    * can join elements with unrelated streams of entities from a database
    * and an ON clause can be specified that will determine which elements from
    * the two streams will be joined together. 
    * 
    * <pre>
    * {@code JinqStream<Country> stream = ...;
    * JinqStream<Pair<Country, Mountain>> result = 
    *    stream.leftOuterJoin(
    *            (c, source) -> source.stream(Mountain.class), 
    *            (country, mountain) -> country.getName().equals(mountain.getCountry()));
    * }
    * </pre>
    * 
    * @see #leftOuterJoin(Join)
    * @param join
    *           function applied to the elements of the stream. When passed an
    *           element from the stream, the function should return a stream of
    *           values that should be paired up with that stream element. The
    *           function must use a JPA association or navigational link as the
    *           base for the stream returned. Both singular or plural
    *           associations are allowed.
    * @param on
    *           this is a comparison function that returns true if the elements
    *           from the two streams should be joined together. It is similar to
    *           a standard where() clause except that the elements from the two
    *           streams are passed in as separate parameters for convenience
    *           (as opposed to being passed in as a pair)
    * @return a new stream with the paired up elements
    */
   public <U> JinqStream<Pair<T, U>> leftOuterJoin(JoinWithSource<T, U> join, WhereForOn<T, U> on);

   
   @FunctionalInterface
   public static interface AggregateGroup<W, U, V> extends Serializable
   {
      public V aggregateSelect(W key, JinqStream<U> val);
   }

   /**
    * Groups together elements from the stream that share a common key.
    * Aggregates can then be calculated over the elements in each group.
    * 
    * <pre>
    * {@code JinqStream<City> stream = ...;
    * JinqStream<Pair<String, Long>> result = 
    *    stream.group(c -> c.getCountry(), (key, cities) -> cities.count());
    * }
    * </pre>
    * 
    * @param select
    *           function applied to each element of the stream that returns the
    *           key to be used to group elements together
    * @param aggregate
    *           function applied to each group and calculates an aggregate value
    *           over the group. The function is passed the key for the group and
    *           a JinqStream of elements contained inside that group. It should
    *           return the aggregate value calculated for that group.
    * @return a new stream containing a tuple for each group. The tuple contains
    *         the key for the group and any calculated aggregate values.
    */
   public <U, V> JinqStream<Pair<U, V>> group(Select<T, U> select,
         AggregateGroup<U, T, V> aggregate);

   /**
    * Calculates two aggregate values instead of one aggregate value for grouped
    * stream elements.
    * 
    * @see #group(Select, AggregateGroup)
    */
   public <U, V, W> JinqStream<Tuple3<U, V, W>> group(Select<T, U> select,
         AggregateGroup<U, T, V> aggregate1, AggregateGroup<U, T, W> aggregate2);

   /**
    * Calculates three aggregate values instead of one aggregate value for
    * grouped stream elements.
    * 
    * @see #group(Select, AggregateGroup)
    */
   public <U, V, W, X> JinqStream<Tuple4<U, V, W, X>> group(
         Select<T, U> select, AggregateGroup<U, T, V> aggregate1,
         AggregateGroup<U, T, W> aggregate2, AggregateGroup<U, T, X> aggregate3);

   /**
    * Calculates four aggregate values instead of one aggregate value for
    * grouped stream elements.
    * 
    * @see #group(Select, AggregateGroup)
    */
   public <U, V, W, X, Y> JinqStream<Tuple5<U, V, W, X, Y>> group(
         Select<T, U> select, AggregateGroup<U, T, V> aggregate1,
         AggregateGroup<U, T, W> aggregate2,
         AggregateGroup<U, T, X> aggregate3, AggregateGroup<U, T, Y> aggregate4);

   // TODO: This interface is a little iffy since the function can potentially
   // return different number types
   // and things can't be checked until runtime, but Java type inferencing
   // currently can't
   // disambiguate between different methods that take functions with different
   // return types.
   // In most cases, this should be fine as long as programmers define V as
   // something specific
   // like Integer or Double instead of something generic like Number.

   // These interfaces are used to define the lambdas used as parameters to
   // various aggregation
   // operations.
   @FunctionalInterface
   public static interface CollectNumber<U, V extends Number & Comparable<V>>
         extends Serializable
   {
      public V aggregate(U val);
   }

   @FunctionalInterface
   public static interface CollectComparable<U, V extends Comparable<V>>
         extends Serializable
   {
      public V aggregate(U val);
   }

   @FunctionalInterface
   public static interface CollectInteger<U> extends CollectNumber<U, Integer>
   {
   }

   @FunctionalInterface
   public static interface CollectLong<U> extends CollectNumber<U, Long>
   {
   }

   @FunctionalInterface
   public static interface CollectDouble<U> extends CollectNumber<U, Double>
   {
   }

   @FunctionalInterface
   public static interface CollectBigDecimal<U> extends
         CollectNumber<U, BigDecimal>
   {
   }

   @FunctionalInterface
   public static interface CollectBigInteger<U> extends
         CollectNumber<U, BigInteger>
   {
   }

   // Having separate sum() methods for different types is messy but due to
   // problems with Java's type inferencing and the fact that JPQL uses
   // different return types for a sum than the types being summed over,
   // this is the only way to do sum operations in a type-safe way.
   /**
    * Calculates a sum over the elements of a stream. Different sum methods are
    * provided for calculating the sum of integer, long, double, BigDecimal, and
    * BigInteger values.
    * 
    * <pre>
    * {@code JinqStream<City> stream = ...;
    * long totalPopulation = stream.sumInteger(c -> c.getPopulation()); 
    * }
    * </pre>
    * 
    * @param aggregate
    *           function applied to each element of the stream. When passed an
    *           element of the stream, it should return the value that should be
    *           added to the sum.
    * @return the sum of the values returned by the function
    */
   public Long sumInteger(CollectInteger<T> aggregate);

   /** @see #sumInteger(CollectInteger) */
   public Long sumLong(CollectLong<T> aggregate);

   /** @see #sumInteger(CollectInteger) */
   public Double sumDouble(CollectDouble<T> aggregate);

   /** @see #sumInteger(CollectInteger) */
   public BigDecimal sumBigDecimal(CollectBigDecimal<T> aggregate);

   /** @see #sumInteger(CollectInteger) */
   public BigInteger sumBigInteger(CollectBigInteger<T> aggregate);

   // TODO: It's more type-safe to have separate maxDouble(), maxDate(), etc.
   // methods,
   // but it's too messy, so I'll provide this simpler max() method for now
   /**
    * Finds the largest or maximum element of a stream.
    * 
    * <pre>
    * {@code JinqStream<Student> stream = ...;
    * Date birthdayOfYoungest = stream.max(s -> s.getBirthday()); 
    * }
    * </pre>
    * 
    * @param aggregate
    *           function applied to each element of the stream. When passed an
    *           element of the stream, it should return the value that should be
    *           compared.
    * @return the maximum of the values returned by the function
    */
   public <V extends Comparable<V>> V max(CollectComparable<T, V> aggregate);

   /**
    * Finds the smallest or minimum element of a stream.
    * 
    * <pre>
    * {@code JinqStream<Student> stream = ...;
    * Date birthdayOfOldest = stream.min(s -> s.getBirthday()); 
    * }
    * </pre>
    * 
    * @see #max(CollectComparable)
    * @param aggregate
    *           function applied to each element of the stream. When passed an
    *           element of the stream, it should return the value that should be
    *           compared.
    * @return the minimum of the values returned by the function
    */
   public <V extends Comparable<V>> V min(CollectComparable<T, V> aggregate);

   /**
    * Finds the average of the elements of a stream.
    * 
    * <pre>
    * {@code JinqStream<Student> stream = ...;
    * double averageAge = stream.avg(s -> s.getage()); 
    * }
    * </pre>
    * 
    * @param aggregate
    *           function applied to each element of the stream. When passed an
    *           element of the stream, it should return the value that should be
    *           included in the average
    * @return the average of the values returned by the function
    */
   public <V extends Number & Comparable<V>> Double avg(
         CollectNumber<T, V> aggregate);

   @FunctionalInterface
   public static interface AggregateSelect<U, V> extends Serializable
   {
      public V aggregateSelect(JinqStream<U> val);
   }

   /**
    * Calculates more than one aggregate function over the elements of the
    * stream.
    * 
    * <pre>
    * {@code JinqStream<City> stream = ...;
    * Pair<Long, Long> result = stream.aggregate(
    *    c -> c.sumInteger(c.getPopulation()),
    *    c -> c.count()); 
    * }
    * </pre>
    * 
    * @param aggregate1
    *           a function that takes a stream and returns the first calculated
    *           aggregate value for the stream
    * @param aggregate2
    *           a function that takes a stream and returns a second calculated
    *           aggregate value for the stream
    * @return a tuple of the calculated aggregate values
    */
   public <U, V> Pair<U, V> aggregate(AggregateSelect<T, U> aggregate1,
         AggregateSelect<T, V> aggregate2);

   /**
    * @see #aggregate(AggregateSelect, AggregateSelect)
    */
   public <U, V, W> Tuple3<U, V, W> aggregate(AggregateSelect<T, U> aggregate1,
         AggregateSelect<T, V> aggregate2, AggregateSelect<T, W> aggregate3);

   /**
    * @see #aggregate(AggregateSelect, AggregateSelect)
    */
   public <U, V, W, X> Tuple4<U, V, W, X> aggregate(
         AggregateSelect<T, U> aggregate1, AggregateSelect<T, V> aggregate2,
         AggregateSelect<T, W> aggregate3, AggregateSelect<T, X> aggregate4);

   /**
    * @see #aggregate(AggregateSelect, AggregateSelect)
    */
   public <U, V, W, X, Y> Tuple5<U, V, W, X, Y> aggregate(
         AggregateSelect<T, U> aggregate1, AggregateSelect<T, V> aggregate2,
         AggregateSelect<T, W> aggregate3, AggregateSelect<T, X> aggregate4,
         AggregateSelect<T, Y> aggregate5);

   /**
    * Sorts the elements of a stream in ascending order based on the value
    * returned. The sort is stable, so it is possible to sort the stream
    * multiple times in order to have multiple sort keys. The last sort becomes
    * the primary sort key, and earlier sorts become lesser keys.
    * 
    * @param sortField
    *           function applied to each element of the stream. When passed an
    *           element of the stream, it should return the value that should be
    *           used as the sorting value of the element
    * @return sorted stream
    */
   public <V extends Comparable<V>> JinqStream<T> sortedBy(
         CollectComparable<T, V> sortField);

   /**
    * Sorts the elements of a stream in descending order based on the value
    * returned.
    * 
    * @see #sortedBy(CollectComparable)
    * @param sortField
    *           function applied to each element of the stream. When passed an
    *           element of the stream, it should return the value that should be
    *           used as the sorting value of the element
    * @return sorted stream
    */
   public <V extends Comparable<V>> JinqStream<T> sortedDescendingBy(
         CollectComparable<T, V> sortField);

   // Overriding the Stream API versions to return a JinqStream instead, so it's
   // easier to chain them
   @Override
   public JinqStream<T> skip(long n);

   @Override
   public JinqStream<T> limit(long n);

   @Override
   public JinqStream<T> distinct();

   /**
    * Counts the elements in the stream. If the stream contains only a single
    * field of data (i.e. not a tuple) as derived from a database query, then
    * the count will be of non-NULL elements only. If the stream contains more
    * than one field of data (i.e. a tuple) or if the stream is streaming
    * in-memory data, then the count will include NULL values.
    * 
    * @see Stream#count()
    */
   @Override
   public long count();

   /**
    * A convenience method for getting the contents of a stream when it contains
    * 0 or 1 values. If the stream contains more than one value, an exception 
    * will be thrown. 
    * 
    * It cannot be used in subqueries.
    *
    * @see Stream#findFirst()
    * 
    * @return an Optional with the single element contained in the stream or
    *            an empty Optional if the stream is empty
    * @throws java.util.NoSuchElementException
    *            stream contains more than one element
    */
   public Optional<T> findOne();

   /**
    * If the stream contains only a single value, this method will return that
    * value. This method is convenient for getting the results of queries that
    * contain only a single value. It is also useful in subqueries.
    * 
    * @return the single element contained in the stream
    * @throws java.util.NoSuchElementException
    *            stream contains zero or more than one element
    */
   public T getOnlyValue();

   /**
    * Convenience method that collects the stream contents into a List.
    * 
    * @return a list of all the elements from the stream
    */
   // TODO: Should toList() throw an exception?
   public List<T> toList();

   /**
    * Returns the query that Jinq will send to the database to generate the
    * values of the stream.
    * 
    * @return the database query string or <code>null</code> if Jinq cannot find
    *         a database query equivalent to the contents of the stream.
    */
   public String getDebugQueryString();

   /**
    * Used for recording an exception that occurred during processing somewhere
    * in the stream chain.
    * 
    * @param source
    *           lambda object that caused the exception (used so that if the
    *           same lambda causes multiple exceptions, only some of them need
    *           to be recorded in order to avoid memory issues)
    * @param exception
    *           actual exception object
    */
   @Deprecated
   public void propagateException(Object source, Throwable exception);

   @Deprecated
   public Collection<Throwable> getExceptions();

   /**
    * Sets a hint on the stream for how the query should be executed
    * 
    * @param name
    *           name of the hint to change
    * @param value
    *           value to assign to the hint
    * @return a pointer to the stream, to make it easier to chain method calls
    *         on the stream
    */
   public JinqStream<T> setHint(String name, Object value);

   /**
    * Easy way to get a JinqStream from a collection.
    */
   public static <U> JinqStream<U> from(Collection<U> collection)
   {
      return new NonQueryJinqStream<>(collection.stream());
   }

   /**
    * Creates a JinqStream containing a single object.
    */
   public static <U> JinqStream<U> of(U value)
   {
      return new NonQueryJinqStream<>(Stream.of(value));
   }
}
