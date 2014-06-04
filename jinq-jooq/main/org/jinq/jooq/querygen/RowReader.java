package org.jinq.jooq.querygen;

import org.jooq.Record;

public interface RowReader<T>
{
   T readResult(Record record);
   T readResult(Record record, int offset);
   int getNumColumns();
}
