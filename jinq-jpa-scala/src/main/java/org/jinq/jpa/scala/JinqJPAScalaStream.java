package org.jinq.jpa.scala;

import org.jinq.orm.stream.scala.JinqScalaStream;

import scala.Function1;
import scala.collection.immutable.List;

public class JinqJPAScalaStream<T> implements JinqScalaStream<T>
{

   @Override
   public List<T> where(Function1<T, Object> fn)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public <U> List<U> select(Function1<T, U> fn)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public List<T> toList()
   {
      // TODO Auto-generated method stub
      return null;
   }
}
