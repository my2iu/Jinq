package ch.epfl.labos.iu.orm.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import ch.epfl.labos.iu.orm.DBSet;
import ch.epfl.labos.iu.orm.DateSorter;
import ch.epfl.labos.iu.orm.DoubleSorter;
import ch.epfl.labos.iu.orm.IntSorter;
import ch.epfl.labos.iu.orm.Pair;
import ch.epfl.labos.iu.orm.QueryComposer;
import ch.epfl.labos.iu.orm.Realizer;
import ch.epfl.labos.iu.orm.StringSorter;
import ch.epfl.labos.iu.orm.VectorSet;
import ch.epfl.labos.iu.orm.DBSet.AggregateDouble;
import ch.epfl.labos.iu.orm.DBSet.AggregateGroup;
import ch.epfl.labos.iu.orm.DBSet.AggregateInteger;
import ch.epfl.labos.iu.orm.DBSet.AggregateSelect;
import ch.epfl.labos.iu.orm.DBSet.Join;
import ch.epfl.labos.iu.orm.DBSet.Select;
import ch.epfl.labos.iu.orm.DBSet.Where;

public class QueryListRealizer<T> implements QueryComposer<T>
{
   public QueryListRealizer(Connection con, SelectFromWhere query, RowReader<T> reader)
   {
      this.con = con;
      this.query = query;
      this.reader = reader;
   }
   
   public SelectFromWhere query;
   public RowReader <T> reader;
   public Connection con;
   
   public VectorSet<T> createRealizedSet()
   {
      VectorSet<T> realizedSet = new VectorSet<T>();
      
      try {
         reader.configureQuery(query);
         PreparedStatement stmt = query.makeQuery(con);
         ResultSet rs = stmt.executeQuery();
         
         while ( rs.next() ) {
            realizedSet.add(reader.readSqlRow(rs));
         }
         rs.close();
         stmt.close();
      } catch(SQLException e)
      {
         e.printStackTrace();
      }
      
      return realizedSet;
   }

   // Dummy versions of query handlers that always return null
   public QueryComposer<T> firstN(int n)
   {
      return null;
   }

   public <U, V> QueryComposer<Pair<U, V>> group(Select<T, U> select,
                                                 AggregateGroup<U, T, V> aggregate)
   {
      return null;
   }

   public <U> QueryComposer<Pair<T, U>> join(Join<T,U> join)
   {
      return null;
   }

   public Double maxDouble(AggregateDouble<T> aggregate)
   {
      return null;
   }

   public Integer maxInt(AggregateInteger<T> aggregate)
   {
      return null;
   }

   public <U> QueryComposer<U> select(Select<T, U> select)
   {
      return null;
   }

   public <U> U selectAggregates(AggregateSelect<T, U> aggregate)
   {
      return null;
   }

   public Double sumDouble(AggregateDouble<T> aggregate)
   {
      return null;
   }

   public Integer sumInt(AggregateInteger<T> aggregate)
   {
      return null;
   }

   public QueryComposer<T> unique()
   {
      return null;
   }

   public QueryComposer<T> where(Where<T> test)
   {
      return null;
   }

   public QueryComposer<T> with(T toAdd)
   {
      return null;
   }

   public QueryComposer<T> sortedByDate(DateSorter<T> sorter,
                                        boolean isAscending)
   {
      return null;
   }

   public QueryComposer<T> sortedByDouble(DoubleSorter<T> sorter,
                                          boolean isAscending)
   {
      return null;
   }

   public QueryComposer<T> sortedByInt(IntSorter<T> sorter, boolean isAscending)
   {
      return null;
   }

   public QueryComposer<T> sortedByString(StringSorter<T> sorter,
                                          boolean isAscending)
   {
      return null;
   }
}
