package ch.epfl.labos.iu.orm.query;

import java.sql.ResultSet;

public interface RowReader<T> 
{
   public T readSqlRow(ResultSet rs);
   public void configureQuery(SelectFromWhere query);
   public RowReader<T> copy();
   
   // TODO: this is technically incorrect--it should return a QueryStringWithParameters
   public String queryString();
}
