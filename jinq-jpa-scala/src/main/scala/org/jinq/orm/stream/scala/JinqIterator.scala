package org.jinq.orm.stream.scala

import java.math.BigDecimal
import java.math.BigInteger
import scala.collection.GenTraversableOnce

/**
 * The JinqIterator is a normal Scala Iterator extended with extra methods. These
 * extra methods are needed because:
 * <ul>
 * <li>They provide extra functionality that more closely mirrors database query
 * functionality
 * <li>They have names that are derived from database query syntax instead of
 * names derived from functional programming syntax
 * </ul>
 *
 * Programmers typically get access to <code>JinqIterator</code>s through a
 * JinqIterator provider. For example, the
 * {@code org.jinq.jpa.JinqJPAScalaIteratorProvider} is able to create iterators of JPA
 * entities from a database. It is also possible to create
 * <code>JinqIterator</code>s from collections and single objects.
 *
 * Similar to a normal Scala iterator, once you have performed
 * some sort of transformation operation on the iterator, you should not
 * perform any more operations on the iterator. The <code>JinqIterator</code>
 * differs from a normal Scala iterator in that it also does not allow you
 * to perform operations on the iterator after you have started iterating over
 * its contents.
 *
 * @tparam T
 *           type of object that is being processed by the stream
 */
trait JinqIterator[T] extends Iterator[T] {
  /**
   * Filters the elements of the stream.
   *
   * {{{
   * val result = iterator.where(c => c.getName() == "Alice");
   * }}}
   *
   *
   * @param fn
   *           function applied to the elements of the iterator. When passed an
   *           element from the iterator, the function should return true if the
   *           element should be kept. if the function returns false, the
   *           element is discarded.
   * @return a new iterator that returns only the elements satisfying the filter
   */
  def where(fn: (T) => Boolean): JinqIterator[T]

  /**
   * Filters the elements of the iterator. This version allows the filter
   * function to take a second parameter with an InQueryStreamSource. This lets
   * the function create new streams of elements that it can use in subqueries.
   *
   * @see #where((T)=>Boolean)
   * @param fn
   *           function applied to each element of the iterator. The function is
   *           passed an element from the iterator as well as an
   *           [[InQueryStreamSource]]. The function should return true if
   *           the element should be kept. if the function returns false, the
   *           element is discarded.
   * @return a new iterator that returns only the elements satisfying the filter
   */
  def where(fn: (T, InQueryStreamSource) => Boolean): JinqIterator[T]

  /**
   * Transforms the elements in the iterator. The method allows you to rewrite
   * each element from the iterator, so that they contain only certain fields or
   * to do some calculation based on the values of the fields.
   *
   * {{{
   * val result = iterator.select(c => c.getName);
   * }}}
   *
   * @param fn
   *           function applied to the elements of the iterator. When passed an
   *           element from the iterator, the function should return a new value
   *           that should be used instead of the element in the iterator.
   * @return a new iterator that uses only the new rewritten iterator elements
   */
  def select[U](fn: (T) => U): JinqIterator[U]

  /**
   * Transforms the elements in the Iterator. This version also passes an
   * [[InQueryStreamSource]] to the select function so that the function
   * can create new iterator of elements to use in subqueries.
   *
   * @see #select((T)=>U)
   */
  def select[U](fn: (T, InQueryStreamSource) => U): JinqIterator[U]

  /**
   * Transforms the elements in the iterator. The method allows you to rewrite
   * each element from the iterator, so that they contain only certain fields or
   * to do some calculation based on the values of the fields. Unlike a normal
   * select(), this method allows you to return a more than one element. The
   * elements will all be added to the final iterator.
   *
   * <pre>
   * {@code val stream : JinqIterator[Country] = ...;
   * val result = stream.selectAll(_.getCities);
   * }
   * </pre>
   *
   * @see #select((T)=>U)
   * @param select
   *           function applied to the elements of the iterator. When passed an
   *           element from the iterator, the function should return an iterator
   *           of new values that will be flattened and placed into the new
   *           iterator
   * @return a new iterator that uses only the new rewritten iterator elements
   */
  def selectAll[U](fn: (T) => GenTraversableOnce[U]): JinqIterator[U]

  /**
   * Transforms the elements in the Iterator. This version also passes an
   * [[InQueryStreamSource]] to the select function so that the function
   * can create new iterator of elements to use in subqueries.
   *
   * @see #selectAll((T)=>JinqIterator[U])
   */
  def selectAll[U](fn: (T, InQueryStreamSource) => GenTraversableOnce[U]): JinqIterator[U]

  /**
   * Pairs up each entry of the iterator with an iterator of related elements.
   *
   * {{{
   * val iterator: JinqIterator[Country] = ...
   * val result = iterator.join(c => c.getCities);
   * }}}
   *
   * @param fn
   *           function applied to the elements of the iterator. When passed an
   *           element from the iterator, the function should return a iterator of
   *           values that should be paired up with that iterator element.
   * @return a new iterator with the paired up elements
   */
  def join[U](fn: (T) => JinqIterator[U]): JinqIterator[(T, U)]

  /**
   * Pairs up each entry of the iterator with an iterator of related elements. This
   * version also passes an [[InQueryStreamSource]] to the join function so
   * that the function can join elements with unrelated iterator of entities
   * from a database.
   *
   * @see #join[U]((T)=>JinqIterator[U])
   */
  def join[U](fn: (T, InQueryStreamSource) => JinqIterator[U]): JinqIterator[(T, U)]

  /**
   * Pairs up each entry of the iterator with an iterator of related elements. Uses
   * a left outer join during the pairing up process, so even if an element is
   * not joined with anything, a pair will still be created in the output
   * iterator consisting of the element paired with null.
   *
   * {{{
   * val iterator: JinqIterator[Country] = ...
   * val result =
   *    iterator.leftOuterJoin(c => c.getMountains);
   * val resultHighest =
   *    iterator.leftOuterJoin(c => JinqIterator.of(c.getHighestMountain));
   * }}}
   *
   * @see #join
   * @param fn
   *           function applied to the elements of the iterator. When passed an
   *           element from the iterator, the function should return an iterator of
   *           values that should be paired up with that iterator element. The
   *           function must use a JPA association or navigational link as the
   *           base for the iterator returned. Both singular or plural
   *           associations are allowed.
   * @return a new iterator with the paired up elements
   */
  def leftOuterJoin[U](fn: (T) => JinqIterator[U]): JinqIterator[Tuple2[T, U]]

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
  def leftOuterJoin[U](join: (T, InQueryStreamSource) => JinqIterator[U], on: (T, U) => Boolean) : JinqIterator[Tuple2[T, U]] 

  
  /**
   * When executing the query, the items referred to be the plural 
   * association will be fetched as well. The stream itself will 
   * still return the same elements though. This reduces the number
   * of database calls needed to fetch things from a database. 
   * 
   * {{{
   * val iterator: JinqIterator[Country] = ...
   * val result =
   *    iterator.joinFetch(_.getCities);
   * }}}
   * 
   * @param join
   *           function applied to the elements of the stream. When passed an
   *           element from the stream, the function should return a stream of
   *           values that should be fetched as well 
   */
  def joinFetch[U](fn: (T) => JinqIterator[U]): JinqIterator[T]

  /**
   * @see #joinFetch 
   */
  def leftOuterJoinFetch[U](fn: (T) => JinqIterator[U]): JinqIterator[T]

  
  /**
   * Groups together elements from the iterator that share a common key.
   * Aggregates can then be calculated over the elements in each group.
   *
   * {{{
   * val iterator : JinqIterator[City] = ...
   * val result =
   *    iterator.group(
   *      c => c.getCountry(),
   *      (key:String, cities) -> cities.count());
   * }}}
   *
   * @param groupingFn
   *           function applied to each element of the iterator that returns the
   *           key to be used to group elements together
   * @param valueFn
   *           function applied to each group and calculates an aggregate value
   *           over the group. The function is passed the key for the group and
   *           a [[JinqIterator]] of elements contained inside that group. It should
   *           return the aggregate value calculated for that group.
   * @return a new iterator containing a tuple for each group. The tuple contains
   *         the key for the group and any calculated aggregate values.
   */
  def group[U, V](groupingFn: (T) => U, valueFn: (U, JinqIterator[T]) => V): JinqIterator[(U, V)]

  /**
   * Calculates two aggregate values instead of one aggregate value for grouped
   * iterator elements.
   *
   * @see #group
   */
  def group[U, V, W](groupingFn: (T) => U, valueFn1: (U, JinqIterator[T]) => V, valueFn2: (U, JinqIterator[T]) => W): JinqIterator[(U, V, W)]

  /**
   * Calculates three aggregate values instead of one aggregate value for
   * grouped iterator elements.
   *
   * @see #group
   */
  def group[U, V, W, X](groupingFn: (T) => U, valueFn1: (U, JinqIterator[T]) => V, valueFn2: (U, JinqIterator[T]) => W, valueFn3: (U, JinqIterator[T]) => X): JinqIterator[(U, V, W, X)]

  /**
   * Calculates four aggregate values instead of one aggregate value for
   * grouped iterator elements.
   *
   * @see #group
   */
  def group[U, V, W, X, Y](groupingFn: (T) => U, valueFn1: (U, JinqIterator[T]) => V, valueFn2: (U, JinqIterator[T]) => W, valueFn3: (U, JinqIterator[T]) => X, valueFn4: (U, JinqIterator[T]) => Y): JinqIterator[(U, V, W, X, Y)]

  /**
   * Counts the elements in the iterator. If the iterator contains only a single
   * field of data (i.e. not a tuple) as derived from a database query, then
   * the count will be of non-NULL elements only. If the iterator contains more
   * than one field of data (i.e. a tuple) or if the iterator is streaming
   * in-memory data, then the count will include NULL values.
   */
  def count(): java.lang.Long

  /**
   * Calculates a sum over the elements of the iterator. Different sum methods are
   * provided for calculating the sum of Int, Long, Double, BigDecimal, and
   * BigInteger values.
   *
   * {{{
   * val iterator : JinqIterator[City] = ...
   * val totalPopulation = iterator.sumInteger(c => c.getPopulation());
   * }}}
   *
   * @param fn
   *           function applied to each element of the iterator. When passed an
   *           element of the iterator, it should return the value that should be
   *           added to the sum.
   * @return the sum of the values returned by the function
   */
  def sumInteger(fn: (T) => java.lang.Integer): java.lang.Long

  /** @see #sumInteger */
  def sumLong(fn: (T) => java.lang.Long): java.lang.Long

  /** @see #sumInteger */
  def sumDouble(fn: (T) => java.lang.Double): java.lang.Double

  /** @see #sumInteger */
  def sumBigDecimal(fn: (T) => BigDecimal): BigDecimal

  /** @see #sumInteger */
  def sumBigInteger(fn: (T) => BigInteger): BigInteger

  /**
   * Finds the largest or maximum element of an iterator.
   *
   * {{{
   * val iterator : JinqIterator[Student] = ...
   * val birthdayOfYoungest = iterator.max(s => s.getBirthday());
   * }}}
   *
   * @param fn
   *           function applied to each element of the iterator. When passed an
   *           element of the iterator, it should return the value that should be
   *           compared.
   * @return the maximum of the values returned by the function
   */
  def max[V <% java.lang.Comparable[V]](fn: (T) => V): V

  /**
   * Finds the smallest or minimum element of an iterator.
   *
   * {{{
   * val iterator : JinqIterator[Student] = ...
   * val birthdayOfOldest = iterator.min(s => s.getBirthday());
   * }}}
   *
   * @param fn
   *           function applied to each element of the iterator. When passed an
   *           element of the iterator, it should return the value that should be
   *           compared.
   * @return the minimum of the values returned by the function
   */
  def min[V <% Comparable[V]](fn: (T) => V): V

  /**
   * Finds the average of the elements of an iterator.
   *
   * {{{
   * val iterator : JinqIterator[Student] = ...
   * val averageAge = iterator.avg(s => s.getage());
   * }}}
   * </pre>
   *
   * @param aggregate
   *           function applied to each element of the iterator. When passed an
   *           element of the iterator, it should return the value that should be
   *           included in the average
   * @return the average of the values returned by the function
   */
  def avg[V](fn: (T) => V)(implicit num: Numeric[V]): java.lang.Double

  /**
   * Calculates more than one aggregate function over the elements of the
   * iterator.
   *
   * {{{
   * val iterator: JinqIterator[City] = ...
   * val result = stream.aggregate(
   *    c => c.sumInteger(c.getPopulation()),
   *    c => c.count());
   * }}}
   *
   * @param fn1
   *           a function that takes an iterator and returns the first calculated
   *           aggregate value for the iterator
   * @param fn2
   *           a function that takes an iterator and returns a second calculated
   *           aggregate value for the iterator
   * @return a tuple of the calculated aggregate values
   */
  def aggregate[U, V](fn1: (JinqIterator[T]) => U, fn2: (JinqIterator[T]) => V): (U, V)

  /** @see #aggregate */
  def aggregate[U, V, W](fn1: (JinqIterator[T]) => U, fn2: (JinqIterator[T]) => V, fn3: (JinqIterator[T]) => W): (U, V, W)

  /** @see #aggregate */
  def aggregate[U, V, W, X](fn1: (JinqIterator[T]) => U, fn2: (JinqIterator[T]) => V, fn3: (JinqIterator[T]) => W, fn4: (JinqIterator[T]) => X): (U, V, W, X)

  /** @see #aggregate */
  def aggregate[U, V, W, X, Y](fn1: (JinqIterator[T]) => U, fn2: (JinqIterator[T]) => V, fn3: (JinqIterator[T]) => W, fn4: (JinqIterator[T]) => X, fn5: (JinqIterator[T]) => Y): (U, V, W, X, Y)

  /**
   * Sorts the elements of the iterator in ascending order based on the value
   * returned. The sort is stable, so it is possible to sort the iterator
   * multiple times in order to have multiple sort keys. The last sort becomes
   * the primary sort key, and earlier sorts become lesser keys.
   *
   * @param fn
   *           function applied to each element of the iterator. When passed an
   *           element of the iterator, it should return the value that should be
   *           used as the sorting value of the element
   * @return sorted iterator
   */
  def sortedBy[V <% java.lang.Comparable[V]](fn: (T) => V): JinqIterator[T] // V should be Comparable, but we can't do that since Scala's primitive values aren't Comparable, and implicit conversion can get confused sometimes

  /**
   * Sorts the elements of the iterator in descending order based on the value
   * returned.
   *
   * @see #sortedBy
   */
  def sortedDescendingBy[V <% java.lang.Comparable[V]](fn: (T) => V): JinqIterator[T]

  /**
   * @param n number of elements that should be returned. Any additional elements will be truncated.
   * @return an iterator that returns only the first n elements
   */
  def limit(n: Long): JinqIterator[T]

  /**
   * @param n number of elements to skip
   * @return an iterator that skips over the first n elements
   */
  def skip(n: Long): JinqIterator[T]

  /**
   * @return an iterator that only holds the unique elements of the iterator.
   *   The element will not return the same element twice.
   */
  def distinct(): JinqIterator[T]

  /**
   * Sets a hint on the iterator for how the query should be executed
   *
   * @param name
   *           name of the hint to change
   * @param value
   *           value to assign to the hint
   * @return a pointer to the iterator, to make it easier to chain method calls
   *         on the iterator
   */

  def setHint(name: String, value: Object): JinqIterator[T]

  /**
   * If the iterator contains only a single value, this method will return that
   * value. This method is convenient for getting the results of queries that
   * contain only a single value. It is also useful in subqueries.
   *
   * @return the single element contained in the iterator
   * @throws java.util.NoSuchElementException
   *            iterator contains zero or more than one element
   */
  def getOnlyValue(): T
}

object JinqIterator {
  /**
   * @return a [[JinqIterator]] over the single element value.
   */
  def of[T](value: T): JinqIterator[T] = {
    new NonQueryJinqIterator(List(value).toIterator, null);
  }
}