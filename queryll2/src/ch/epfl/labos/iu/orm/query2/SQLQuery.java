package ch.epfl.labos.iu.orm.query2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.jinq.tuples.Pair;

public abstract class SQLQuery<T> implements SQLComponent
{
   public SQLReader<T> reader;
   String[] colAliases;
   public abstract SQLQuery<T> precopy(Map<Object, Object> remap);
   public abstract void postcopy(Map<Object, Object> remap);
   
   public SQLQuery(SQLReader<T> reader)
   {
      this.reader = reader;
   }
   
   public SQLReader<T> getReader() 
   {
      return reader;
   }

   public void prepareQuery(JDBCQuerySetup setup)
      throws QueryGenerationException
   {
      colAliases = new String[getNumColumns()];
      for (int n = 0; n < getNumColumns(); n++)
         colAliases[n] = setup.nextColAlias();
   }

   public void storeParamLinks(int lambdaIndex, List<ParameterLocation> params) 
      throws QueryGenerationException
   {
   }

   public int getNumColumns() 
   {
      return reader.getNumColumns();
   }
   
   public String getColAlias(int colIndex)
   {
      return colAliases[colIndex];
   }
   
   public SQLQuery<T> copy()
   {
      Map<Object, Object> remap = new HashMap<Object, Object>();
      SQLQuery<T> toReturn = precopy(remap);
      toReturn.postcopy(remap);
      return toReturn;
   }
   
   // A SelectFromWhere, SelectFromWhereGroup, or SortedAndLimited versions
   // of them have columns. As the query is built up, these values in these
   // columns may be reused in other parts of a query, so this gives a consistent
   // interface to them.
   // TODO: provide a common interface for doing joins
   // TODO: InternalGroup should export this interface as well?
   public static interface SelectFromWhereColumns
   {
      public List<SQLFragment> getColumns();
   }
   
   public static class SelectFromWhere<T> extends SQLQuery<T> implements SelectFromWhereColumns
   {
      public List<SQLFragment> columns = new Vector<SQLFragment>();
      public SQLFragment where = new SQLFragment();
      public List<SQLFrom> from = new Vector<SQLFrom>();
      public Map<N111LinkDescription, SQLSubstitution.FromReference> cachedN111NavigationalLinks = 
         new HashMap<N111LinkDescription, SQLSubstitution.FromReference>(); 

      public SelectFromWhere(SQLReader<T> reader) {super(reader);}
      public SelectFromWhere(SQLReader<T> reader, String[] columns, String table) 
      {
         super(reader);
         SQLFrom entityTable = SQLFrom.fromTable(table);
         for (String col: columns)
         {
            SQLFragment fragment = new SQLFragment();
            fragment.add(new SQLSubstitution.FromReference(entityTable));
            fragment.add("." + col);
            this.columns.add(fragment);
         }
         this.from.add(entityTable);
      }

      public List<SQLFragment> getColumns() { return columns; }

      public JDBCFragment generateQuery(JDBCQuerySetup setup)
         throws QueryGenerationException
      {
         JDBCFragment toReturn = new JDBCFragment();
         toReturn.add("SELECT ");
         for (int n = 0; n < columns.size(); n++)
         {
            SQLFragment col = columns.get(n);
            if (n != 0) toReturn.add(", ");
            toReturn.add(col.generateQuery(setup));
            toReturn.add(" AS " + getColAlias(n));
         }
         toReturn.add(" FROM ");
         boolean isFirst = true;
         for (SQLFrom f: from)
         {
            if (!isFirst) toReturn.add(", ");
            isFirst = false;
            toReturn.add(f.generateQuery(setup));
         }
         if (!where.isEmpty())
         {
            toReturn.add(" WHERE ");
            toReturn.add(where.generateQuery(setup));
         }
         return toReturn;
      }
      public void prepareQuery(JDBCQuerySetup setup)
         throws QueryGenerationException
      {
         super.prepareQuery(setup);
         where.prepareQuery(setup);
         for (SQLFragment col: columns)
            col.prepareQuery(setup);
         for (SQLFrom f: from)
            f.prepareQuery(setup);
      }
      public void storeParamLinks(int lambdaIndex, List<ParameterLocation> params) 
         throws QueryGenerationException
      {
         super.storeParamLinks(lambdaIndex, params);
         where.storeParamLinks(lambdaIndex, params);
         for (SQLFragment col: columns)
            col.storeParamLinks(lambdaIndex, params);
         for (SQLFrom f: from)
            f.storeParamLinks(lambdaIndex, params);
      }

      public SQLQuery<T> precopy(Map<Object, Object> remap)
      {
         if (remap.containsKey(this))  // This should never happen?
            return (SQLQuery<T>)remap.get(this);
         SelectFromWhere<T> toReturn = new SelectFromWhere<T>(reader);
         remap.put(this, toReturn);
         toReturn.where = where.precopy(remap);
         for (SQLFragment col: columns)
            toReturn.columns.add(col.precopy(remap));
         for (SQLFrom f: from)
            toReturn.from.add(f.precopy(remap));
         for (Map.Entry<N111LinkDescription, SQLSubstitution.FromReference> entry: cachedN111NavigationalLinks.entrySet())
            toReturn.cachedN111NavigationalLinks.put(entry.getKey().precopy(remap), (SQLSubstitution.FromReference)entry.getValue().precopy(remap));
         return toReturn;
      }
      public void postcopy(Map<Object, Object> remap)
      {
         where.postcopy(remap);
         for (SQLFragment col: columns)
            col.postcopy(remap);
         for (SQLFrom f: from)
            f.postcopy(remap);
         // The hashes may change after a postcopy, so we need to rebuild the hash map
         Map<N111LinkDescription, SQLSubstitution.FromReference> newN111LinkCache = 
            new HashMap<N111LinkDescription, SQLSubstitution.FromReference>(); 
         for (Map.Entry<N111LinkDescription, SQLSubstitution.FromReference> entry: cachedN111NavigationalLinks.entrySet())
         {
            entry.getKey().postcopy(remap);
            entry.getValue().postcopy(remap);
            newN111LinkCache.put(entry.getKey(), entry.getValue());
         }
         cachedN111NavigationalLinks = newN111LinkCache;
      }
      public SQLSubstitution.FromReference findCachedN111Link(String fromEntity, String name, List<SQLFragment> keyColumns)
      {
         return cachedN111NavigationalLinks.get(new N111LinkDescription(fromEntity, name, keyColumns));
      }
      public void addCachedN111Link(String fromEntity, String name, List<SQLFragment> keyColumns, SQLSubstitution.FromReference entityTable)
      {
         SQLSubstitution.FromReference oldResult = cachedN111NavigationalLinks.put(new N111LinkDescription(fromEntity, name, keyColumns), entityTable);
         assert(oldResult == null);
      }
   }
   public static class SelectFromWhereGroup<U,V> extends SQLQuery<Pair<U,V>> implements SelectFromWhereColumns
   {
      public SelectFromWhere<Pair<U,V>> sfw;
      public List<SQLFragment> groupColumns = new Vector<SQLFragment>();

      public SelectFromWhereGroup(SQLReader<Pair<U,V>> reader) {super(reader);}
      
      public List<SQLFragment> getColumns() { return sfw.columns; }

      public JDBCFragment generateQuery(JDBCQuerySetup setup)
         throws QueryGenerationException
      {
         JDBCFragment toReturn = sfw.generateQuery(setup);
         toReturn.add(" GROUP BY ");
         for (int n = 0; n < groupColumns.size(); n++)
         {
            SQLFragment col = groupColumns.get(n);
            if (n != 0) toReturn.add(", ");
            toReturn.add(col.generateQuery(setup));
//            toReturn.add(" AS " + getColAlias(n));
         }
         return toReturn;
      }
      public void prepareQuery(JDBCQuerySetup setup)
         throws QueryGenerationException
      {
         super.prepareQuery(setup);
         sfw.prepareQuery(setup);
         for (SQLFragment g: groupColumns)
            g.prepareQuery(setup);
      }

      public void storeParamLinks(int lambdaIndex, List<ParameterLocation> params) 
         throws QueryGenerationException
      {
         super.storeParamLinks(lambdaIndex, params);
         sfw.storeParamLinks(lambdaIndex, params);
         for (SQLFragment g: groupColumns)
            g.storeParamLinks(lambdaIndex, params);
      }

      public SQLQuery<Pair<U,V>> precopy(Map<Object, Object> remap)
      {
         if (remap.containsKey(this))  // This should never happen?
            return (SQLQuery<Pair<U,V>>)remap.get(this);
         SelectFromWhereGroup<U,V> toReturn = new SelectFromWhereGroup<U,V>(reader);
         remap.put(this, toReturn);
         toReturn.sfw = (SelectFromWhere<Pair<U, V>>) sfw.precopy(remap);
         for (SQLFragment col: groupColumns)
            toReturn.groupColumns.add(col.precopy(remap));
         return toReturn;
      }
      public void postcopy(Map<Object, Object> remap)
      {
         sfw.postcopy(remap);
         for (SQLFragment col: groupColumns)
            col.postcopy(remap);
      }
      
   }
   
   // Used internally to represent groupings of records for which
   // aggregates will be calculated
   public static class InternalGroup<T> extends SQLQuery<T>
   {
      public List<SQLFragment> columns = new Vector<SQLFragment>();
//      public SQLFragment where = new SQLFragment();
//      public List<SQLFrom> from = new Vector<SQLFrom>();

      public InternalGroup(SQLReader<T> reader) {super(reader);}
      
      public JDBCFragment generateQuery(JDBCQuerySetup setup)
         throws QueryGenerationException
      {
         throw new QueryGenerationException("Cannot create queries based on internal structures");
      }
      public void prepareQuery(JDBCQuerySetup setup)
         throws QueryGenerationException
      {
         throw new QueryGenerationException("Cannot create queries based on internal structures");
      }

      public void storeParamLinks(int lambdaIndex, List<ParameterLocation> params) 
         throws QueryGenerationException
      {
         throw new QueryGenerationException("Internal groups have no parameters");
      }

      public SQLQuery<T> precopy(Map<Object, Object> remap)
      {
         if (remap.containsKey(this))  // This should never happen?
            return (SQLQuery<T>)remap.get(this);
         InternalGroup<T> toReturn = new InternalGroup<T>(reader);
         remap.put(this, toReturn);
         for (SQLFragment col: columns)
            toReturn.columns.add(col.precopy(remap));
         return toReturn;
      }
      public void postcopy(Map<Object, Object> remap)
      {
         for (SQLFragment col: columns)
            col.postcopy(remap);
      }
   }
   
   // Once a query is sorted and/or limited, it can no longer be used as a 
   // subquery. Only SelectFromWhere and SelectFromWhereGroup queries can be
   // sorted and limited, I think
   public static class SortedAndLimited<U> extends SQLQuery<U> implements SelectFromWhereColumns
   {
      public SQLQuery<U> sfw;  // This can be a SelectFromWhere or a SelectFromWhereGroup
      public List<SQLFragment> sortColumns = new ArrayList<SQLFragment>();
      public List<Boolean> sortIsAscending = new ArrayList<Boolean>();
      public int firstN = -1;
      
      public SortedAndLimited(SQLReader<U> reader) {super(reader);}
      
      public List<SQLFragment> getColumns() { return ((SelectFromWhereColumns)sfw).getColumns(); }
      public JDBCFragment generateQuery(JDBCQuerySetup setup)
         throws QueryGenerationException
      {
         JDBCFragment toReturn = sfw.generateQuery(setup);
         if (!sortColumns.isEmpty())
         {
            toReturn.add(" ORDER BY ");
            for (int n = sortColumns.size() - 1; n >= 0; n--)
            {
               SQLFragment col = sortColumns.get(n);
               if (n != sortColumns.size() - 1) toReturn.add(", ");
               toReturn.add(col.generateQuery(setup));
               if (sortIsAscending.get(n))
                  toReturn.add(" ASC");
               else
                  toReturn.add(" DESC");
            }
         }
         if (firstN != -1)
         {
            toReturn.add(" LIMIT " + firstN);
         }
         return toReturn;
      }
      public void prepareQuery(JDBCQuerySetup setup)
         throws QueryGenerationException
      {
         super.prepareQuery(setup);
         sfw.prepareQuery(setup);
         for (SQLFragment s: sortColumns)
            s.prepareQuery(setup);
      }
      public void storeParamLinks(int lambdaIndex, List<ParameterLocation> params) 
         throws QueryGenerationException
      {
         super.storeParamLinks(lambdaIndex, params);
         sfw.storeParamLinks(lambdaIndex, params);
         for (SQLFragment s: sortColumns)
            s.storeParamLinks(lambdaIndex, params);
      }

      public SQLQuery<U> precopy(Map<Object, Object> remap)
      {
         if (remap.containsKey(this))  // This should never happen?
            return (SQLQuery<U>)remap.get(this);
         SortedAndLimited<U> toReturn = new SortedAndLimited<U>(reader);
         remap.put(this, toReturn);
         toReturn.sfw = sfw.precopy(remap);
         for (SQLFragment col: sortColumns)
            toReturn.sortColumns.add(col.precopy(remap));
         for (Boolean isAscending: sortIsAscending)
            toReturn.sortIsAscending.add(isAscending);
         toReturn.firstN = firstN;
         return toReturn;
      }
      public void postcopy(Map<Object, Object> remap)
      {
         sfw.postcopy(remap);
         for (SQLFragment col: sortColumns)
            col.postcopy(remap);
      }
   }
   
   public static class N111LinkDescription
   {
      public String fromEntity;
      public String name;
      public List<SQLFragment> base = new ArrayList<SQLFragment>();  // key constraint off which link is based off of
      public N111LinkDescription() {}
      public N111LinkDescription(String fromEntity, String name, List<SQLFragment> keyColumns)
      {
         this.fromEntity = fromEntity;
         this.name = name;
         this.base.addAll(keyColumns);
      }
      @Override public int hashCode()
      {
         return fromEntity.hashCode() ^ name.hashCode() ^ base.hashCode();
      }
      
      public boolean equals(Object o)
      {
         if (!(o instanceof N111LinkDescription)) return false;
         N111LinkDescription other = (N111LinkDescription)o;
         if (!fromEntity.equals(other.fromEntity)) return false;
         if (!name.equals(other.name)) return false;
         if (base.size() != other.base.size()) return false;
         for (int n = 0; n < base.size(); n++)
            if (!base.get(n).equals(other.base.get(n))) return false;
         return true;
      }
      public N111LinkDescription precopy(Map<Object, Object> remap)
      {
         if (remap.containsKey(this))  // This should never happen?
            return (N111LinkDescription)remap.get(this);
         N111LinkDescription toReturn = new N111LinkDescription();
         toReturn.name = name;
         toReturn.fromEntity = fromEntity;
         List<SQLFragment> newBase = new ArrayList<SQLFragment>();
         for (SQLFragment col: base)
            newBase.add(col.precopy(remap));
         toReturn.base = newBase;
         return toReturn;
      }
      public void postcopy(Map<Object, Object> remap)
      {
         for (SQLFragment col: base)
            col.postcopy(remap);
      }
   }
}
