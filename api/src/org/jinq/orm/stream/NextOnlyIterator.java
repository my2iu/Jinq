package org.jinq.orm.stream;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Creating things that act like iterators is annoying because the
 * programmer has to supply implementations of the hasNext() and next()
 * methods. This class is similar to the GUAVA AbstractIterator. 
 * 
 * @param <T>
 */
public abstract class NextOnlyIterator<T> implements Iterator<T>
{
   // If both hasMore and isEof are false, then the
   // iterator does not know what is coming up next. If
   // hasMore is true, then the iterator has peeked at the
   // next element, and that element is stored in peek.
   boolean hasMore = false;
   boolean isEof = false;
   T peek;

   @Override
   public boolean hasNext()
   {
      if (!hasMore && !isEof)
         generateNext();
      if (hasMore) return true;
      if (isEof) return false;
      throw new IllegalArgumentException("generateNext() did not find a new element");
   }

   @Override
   public T next()
   {
      if (!hasNext()) throw new NoSuchElementException();
      hasMore = false;
      return peek;
   }
   
   /**
    * This method will be called by the iterator whenever it needs
    * to find the next element to be iterated over. Each time 
    * generateNext() is called, it should call nextElement() or 
    * noMoreElements() as appropriate.
    */
   protected abstract void generateNext();
   
   /**
    * Call this from generateNext() with the next element to
    * be returned from the iterator.
    * @param el
    */
   protected void nextElement(T el)
   {
      assert(!hasMore && !isEof);
      peek = el;
      hasMore = true;
   }

   /**
    * Call this from generateNext() if there are no more elements
    * to be iterated over.
    */
   protected void noMoreElements()
   {
      assert(!hasMore && !isEof);
      isEof = true;
   }
}
