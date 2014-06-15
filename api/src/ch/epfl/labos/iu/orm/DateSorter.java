package ch.epfl.labos.iu.orm;

import java.sql.Date;

public interface DateSorter<T> extends java.io.Serializable
{
   public Date value(T val);
}
