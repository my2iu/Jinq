package org.jinq.orm.stream;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import ch.epfl.labos.iu.orm.QueryComposer;

public class QueryJinqStream<T> extends LazyWrappedStream<T> implements JinqStream<T>
{
   QueryComposer<T> query;
   public QueryJinqStream(QueryComposer<T> query)
   {
      this.query = query;
   }
   
   protected Stream<T> createWrappedStream() 
   {
      return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(query.executeAndReturnResultIterator(), Spliterator.CONCURRENT), 
            false);
   }
}
