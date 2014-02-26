package ch.epfl.labos.iu.orm;

import ch.epfl.labos.iu.orm.query.RowReader;
import ch.epfl.labos.iu.orm.query.SelectFromWhere;

public interface DoubleSorter<T> extends java.io.Serializable
{
   public double value(T val);
}
