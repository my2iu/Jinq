package org.jinq.jpa.transform;

public class QueryTransformException extends Exception
{
   public QueryTransformException(String msg)
   {
      super(msg);
   }

   public QueryTransformException(Throwable e)
   {
      super(e);
   }

   public QueryTransformException(String msg, Throwable e)
   {
      super(msg, e);
   }

}
