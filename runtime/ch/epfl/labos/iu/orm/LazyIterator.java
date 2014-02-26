/**
 * 
 */
package ch.epfl.labos.iu.orm;

import java.util.Iterator;

public class LazyIterator<T> implements Iterator<T>
{
   public LazySet<T> parent;
   LazyIterator(LazySet<T> parent)
   {
      this.parent = parent;
   }
   Iterator<T> realIterator = null;
   public boolean hasNext()
   {
      if (realIterator == null)
      {
         parent.realize();
         realIterator = parent.data.iterator();
      }
      return realIterator.hasNext();
   }

   public T next()
   {
      if (realIterator == null)
      {
         parent.realize();
         realIterator = parent.data.iterator();
      }
      return realIterator.next();
   }

   public void remove()
   {
      if (realIterator == null)
      {
         parent.realize();
         realIterator = parent.data.iterator();
      }
      realIterator.remove();
   }
}