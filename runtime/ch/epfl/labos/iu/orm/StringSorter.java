package ch.epfl.labos.iu.orm;


public interface StringSorter<T> extends java.io.Serializable
{
   public String value(T val);
}
