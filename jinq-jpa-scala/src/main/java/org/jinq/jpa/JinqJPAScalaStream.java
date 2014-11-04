package org.jinq.jpa;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jinq.jpa.scala.JavaToScalaConverters;
import org.jinq.jpa.transform.ScalaJoinTransform;
import org.jinq.orm.stream.InQueryStreamSource;
import org.jinq.orm.stream.scala.JinqScalaStream;

import scala.Function1;
import scala.Function2;
import scala.Tuple2;
import scala.collection.Iterator;
import scala.collection.immutable.List;


public class JinqJPAScalaStream<T> implements JinqScalaStream<T>
{
   private static final String GENERIC_TRANSLATION_FAIL_MESSAGE = "Could not translate Scala code to a query";
   JPAQueryComposer<T> queryComposer;
   InQueryStreamSource inQueryStreamSource;
   
   public JinqJPAScalaStream(JPAQueryComposer<T> query)
   {
      this(query, null);
   }
   
   public JinqJPAScalaStream(JPAQueryComposer<T> query, InQueryStreamSource inQueryStreamSource)
   {
      this.inQueryStreamSource = inQueryStreamSource;
      this.queryComposer = query;
   }

   @Override
   public JinqJPAScalaStream<T> where(Function1<T, Object> fn)
   {
      JPAQueryComposer<T> newComposer = queryComposer.where(fn);
      if (newComposer != null) return new JinqJPAScalaStream<T>(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
   }

   @Override
   public <U> JinqJPAScalaStream<U> select(Function1<T, U> fn)
   {
      JPAQueryComposer<U> newComposer = queryComposer.select(fn);
      if (newComposer != null) return new JinqJPAScalaStream<U>(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
   }

   @Override
   public <U> JinqScalaStream<Tuple2<T, U>> join(
         Function1<T, JinqScalaStream<U>> fn)
   {
      JPAQueryComposer<Tuple2<T, U>> newComposer = queryComposer.applyTransformWithLambda(new ScalaJoinTransform(queryComposer.getConfig(), false), fn);
      if (newComposer != null) return new JinqJPAScalaStream<Tuple2<T, U>>(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
   }

   @Override
   public <U> JinqScalaStream<Tuple2<T, U>> join(
         Function2<T, org.jinq.orm.stream.scala.InQueryStreamSource, JinqScalaStream<U>> fn)
   {
      JPAQueryComposer<Tuple2<T, U>> newComposer = queryComposer.applyTransformWithLambda(new ScalaJoinTransform(queryComposer.getConfig(), true), fn);
      if (newComposer != null) return new JinqJPAScalaStream<Tuple2<T, U>>(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
   }

   @Override
   public List<T> toList()
   {
      return JavaToScalaConverters.javaListToList(
            StreamSupport.stream(
                  Spliterators.spliteratorUnknownSize(
                        queryComposer.executeAndReturnResultIterator( err -> {} ), 
                        Spliterator.CONCURRENT), 
                  false).collect(Collectors.toList()));
   }

   @Override
   public Iterator<T> toIterator()
   {
      return JavaToScalaConverters.javaIteratorToIterator(
            queryComposer.executeAndReturnResultIterator( err -> {} ));
   }

}
