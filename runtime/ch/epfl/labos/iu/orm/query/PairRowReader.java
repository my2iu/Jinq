package ch.epfl.labos.iu.orm.query;

import java.sql.ResultSet;
import ch.epfl.labos.iu.orm.Pair;

public class PairRowReader <T, U> implements RowReader<Pair<T, U>>, TupleRowReaderGet1<T>, TupleRowReaderGet2<U>
{
   RowReader<T> one;
   RowReader<U> two;

   public RowReader<U> getTwo()
   {
      return two;
   }
   public RowReader<T> getOne()
   {
      return one;
   }
   public PairRowReader(RowReader<T> one, RowReader<U> two)
   {
      this.one = one;
      this.two = two;
   }
   
   public Pair<T, U> readSqlRow(ResultSet rs)
   {
      return new Pair<T, U>(one.readSqlRow(rs), two.readSqlRow(rs));
   }

   public void configureQuery(SelectFromWhere query)
   {
      one.configureQuery(query);
      two.configureQuery(query);
   }
   public RowReader<Pair<T, U>> copy()
   {
      PairRowReader <T, U> clone = new PairRowReader<T, U>(one, two);
      return clone;
   }
   
   public String queryString()
   {
      assert(false);
      return "ERR";
   }
}
