package ch.epfl.labos.iu.orm;

import ch.epfl.labos.iu.orm.query.RowReader;
import ch.epfl.labos.iu.orm.query.SelectFromWhere;

public interface IntSorter<T> extends java.io.Serializable
{
   public int value(T val);
}
