package org.jinq.jpa;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jinq.jpa.scala.JavaToScalaConverters;
import org.jinq.orm.internal.QueryComposer;
import org.jinq.orm.stream.InQueryStreamSource;
import org.jinq.orm.stream.scala.JinqScalaStream;

import scala.Function1;
import scala.collection.Iterator;
import scala.collection.immutable.List;


public class JinqJPAScalaStream<T> implements JinqScalaStream<T>
{
   private static final String GENERIC_TRANSLATION_FAIL_MESSAGE = "Could not translate Scala code to a query";
   QueryComposer<T> queryComposer;
   InQueryStreamSource inQueryStreamSource;
   
   public JinqJPAScalaStream(QueryComposer<T> query)
   {
      this(query, null);
   }
   
   public JinqJPAScalaStream(QueryComposer<T> query, InQueryStreamSource inQueryStreamSource)
   {
      this.inQueryStreamSource = inQueryStreamSource;
      this.queryComposer = query;
   }

   @Override
   public JinqJPAScalaStream<T> where(Function1<T, Object> fn)
   {
      QueryComposer<T> newComposer = queryComposer.where(fn);
      if (newComposer != null) return new JinqJPAScalaStream<T>(newComposer, inQueryStreamSource);
      throw new IllegalArgumentException(GENERIC_TRANSLATION_FAIL_MESSAGE);
   }

   @Override
   public <U> JinqJPAScalaStream<U> select(Function1<T, U> fn)
   {
      QueryComposer<U> newComposer = queryComposer.select(fn);
      if (newComposer != null) return new JinqJPAScalaStream<U>(newComposer, inQueryStreamSource);
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
