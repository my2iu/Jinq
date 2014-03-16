package org.jinq.orm.stream;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import ch.epfl.labos.iu.orm.DBSet;
import ch.epfl.labos.iu.orm.QueryComposer;
import ch.epfl.labos.iu.orm.QueryList;
import ch.epfl.labos.iu.orm.DBSet.Select;
import ch.epfl.labos.iu.orm.DBSet.Where;

public class QueryJinqStream<T> extends NonQueryJinqStream<T> implements JinqStream<T>
{
   QueryComposer<T> queryComposer;
   public QueryJinqStream(QueryComposer<T> query)
   {
      this.queryComposer = query;
   }
   
   protected Stream<T> createWrappedStream() 
   {
      return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(queryComposer.executeAndReturnResultIterator(), Spliterator.CONCURRENT), 
            false);
   }

   @Override
   public JinqStream<T> where(final Where<T> test)
   {
      QueryComposer<T> newComposer = queryComposer.where(test);
      if (newComposer != null) return new QueryJinqStream<T>(newComposer);
      return super.where(test);
   }

   @Override
   public <U> JinqStream<U> select(Select<T, U> select)
   {
      QueryComposer<U> newComposer = queryComposer.select(select);
      if (newComposer != null) return new QueryJinqStream<U>(newComposer);
      return super.select(select);
   }
}
