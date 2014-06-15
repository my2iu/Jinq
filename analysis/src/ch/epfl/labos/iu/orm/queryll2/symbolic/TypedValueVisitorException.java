package ch.epfl.labos.iu.orm.queryll2.symbolic;

public class TypedValueVisitorException extends Exception
{
   public TypedValueVisitorException() {}
   public TypedValueVisitorException(String msg) { super(msg); }
   public TypedValueVisitorException(String msg, Throwable cause) { super(msg, cause); }
   public TypedValueVisitorException(Throwable cause) { super(cause); }
}
