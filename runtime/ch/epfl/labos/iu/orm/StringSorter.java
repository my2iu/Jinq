package ch.epfl.labos.iu.orm;

import ch.epfl.labos.iu.orm.query.RowReader;
import ch.epfl.labos.iu.orm.query.SelectFromWhere;

public interface StringSorter<T> extends java.io.Serializable
{
   public String value(T val);
}
