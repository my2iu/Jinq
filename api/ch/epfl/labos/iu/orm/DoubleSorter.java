package ch.epfl.labos.iu.orm;


public interface DoubleSorter<T> extends java.io.Serializable
{
   public double value(T val);
}
