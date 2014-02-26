package ch.epfl.labos.iu.orm.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import ch.epfl.labos.iu.orm.Expand;
import ch.epfl.labos.iu.orm.Filter;
import ch.epfl.labos.iu.orm.Iterate;
import ch.epfl.labos.iu.orm.LazySet;
import ch.epfl.labos.iu.orm.QueryList;
import ch.epfl.labos.iu.orm.Replace;
import ch.epfl.labos.iu.orm.DBSet;
import ch.epfl.labos.iu.orm.Unique;
import ch.epfl.labos.iu.orm.VectorSet;

public class QueryLazySet<T> extends QueryList<T>
{
   public QueryLazySet(Connection con, SelectFromWhere query, RowReader<T> reader)
   {
      setQueryComposer(new QueryListRealizer(con, query, reader));
   }
   
   QueryLazySet(QueryListRealizer<T> newRealizer)
   {
      setQueryComposer(newRealizer);
   }
   

//   public DBSet<T> apply(Filter<T> transform)
//   {
//      if (data == null)
//      {
//         System.out.println(transform.getClass().getName());
//         ModifyQueryLazySet transformOptimization = queryTransforms.get(transform.getClass());
//         if (transformOptimization != null)
//         {
//            return new QueryLazySet<T>((QueryListRealizer<T>) transformOptimization.applyTransform((QueryListRealizer)getRealizer()));
//         }
//         else
//         {
//            return super.apply(transform);
//         }
//      }
//      else
//         return super.apply(transform);
//   }
//   
//   public <To> DBSet<To> apply(Expand<T, To> transform)
//   {
//      return super.apply(transform);
//   }
//
//   public <To> DBSet<To> apply(Replace<T, To> transform)
//   {
//      return super.apply(transform);
//   }
//
//   public <Key> DBSet<T> apply(Unique<T, Key> unique)
//   {
//      return super.apply(unique);
//   }
//
//   public void apply(Iterate<T> transform)
//   {
//      super.apply(transform);
//   }
//   
//   
//   // TODO: Dump this stuff
   public static Map<Class, ModifyQueryLazySet> queryTransforms;
   
   public interface ModifyQueryLazySet
   {
      QueryListRealizer<?> applyTransform(QueryListRealizer<?> realizer);
   }
   
}
