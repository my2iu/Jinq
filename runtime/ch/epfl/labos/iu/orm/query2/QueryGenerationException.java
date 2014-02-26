package ch.epfl.labos.iu.orm.query2;

public class QueryGenerationException extends Exception
{
   public QueryGenerationException() {}
   public QueryGenerationException(String msg) { super(msg); }
   public QueryGenerationException(String msg, Throwable cause) { super(msg, cause); }
   public QueryGenerationException(Throwable cause) { super(cause); }

}
