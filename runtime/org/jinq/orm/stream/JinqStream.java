package org.jinq.orm.stream;

import java.util.stream.Stream;

import ch.epfl.labos.iu.orm.DBSet.Select;
import ch.epfl.labos.iu.orm.DBSet.Where;

public interface JinqStream<T> extends Stream<T>
{
   public JinqStream<T> where(Where<T> test);
   public <U> JinqStream<U> select(Select<T, U> select);
}
