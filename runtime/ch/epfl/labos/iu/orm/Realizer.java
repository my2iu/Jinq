/**
 * 
 */
package ch.epfl.labos.iu.orm;

public interface Realizer<U, T> {
   public VectorSet<U> createRealizedSet();
}