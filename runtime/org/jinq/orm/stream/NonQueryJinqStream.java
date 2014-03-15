package org.jinq.orm.stream;

import java.util.stream.Stream;

import ch.epfl.labos.iu.orm.QueryComposer;
import ch.epfl.labos.iu.orm.DBSet.Select;
import ch.epfl.labos.iu.orm.DBSet.Where;

public class NonQueryJinqStream<T> extends LazyWrappedStream<T> implements JinqStream<T>
{
   public NonQueryJinqStream(Stream<T> wrapped)
   {
      super(wrapped);
   }

   @Override
   public JinqStream<T> where(Where<T> test)
   {
      return new NonQueryJinqStream<>(filter( val -> test.where(val) ));
   }

   @Override
   public <U> JinqStream<U> select(Select<T, U> select)
   {
      return new NonQueryJinqStream<>(map( val -> select.select(val) ));
   }

}
