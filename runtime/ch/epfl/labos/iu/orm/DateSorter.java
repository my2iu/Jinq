package ch.epfl.labos.iu.orm;

import java.sql.Date;
import ch.epfl.labos.iu.orm.query.RowReader;
import ch.epfl.labos.iu.orm.query.SelectFromWhere;

public interface DateSorter<T> extends java.io.Serializable
{
   public Date value(T val);
}
