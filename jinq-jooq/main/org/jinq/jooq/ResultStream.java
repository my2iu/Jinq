package org.jinq.jooq;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jinq.orm.stream.LazyWrappedStream;

public class ResultStream<T> extends LazyWrappedStream<T>
{
   ResultStream(Stream<T> wrappedStream)
   {
      super(wrappedStream);
   }

   public List<T> toList()
   {
      return collect(Collectors.toList());
   }
}
