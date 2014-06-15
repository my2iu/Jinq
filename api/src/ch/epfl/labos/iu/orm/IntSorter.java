package ch.epfl.labos.iu.orm;


public interface IntSorter<T> extends java.io.Serializable
{
   public int value(T val);
}
