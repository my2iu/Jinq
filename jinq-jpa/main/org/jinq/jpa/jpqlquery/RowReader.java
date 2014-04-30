package org.jinq.jpa.jpqlquery;

public interface RowReader<T>
{
   T readResult(Object result);
}
