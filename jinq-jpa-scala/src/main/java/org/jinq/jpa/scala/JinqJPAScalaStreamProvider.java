package org.jinq.jpa.scala;

import javax.persistence.EntityManager;

import org.jinq.orm.stream.scala.JinqScalaStream;

public class JinqJPAScalaStreamProvider
{
   public <U> JinqScalaStream<U> streamAll(final EntityManager em, Class<U> entity)
   {
      return new JinqJPAScalaStream<>();
   }
}
