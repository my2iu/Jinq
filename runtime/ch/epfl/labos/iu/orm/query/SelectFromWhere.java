package ch.epfl.labos.iu.orm.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Vector;

public class SelectFromWhere implements Cloneable
{
   int nextTableNumber = 0;
   static final String TableLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
   
   Vector<String> tables = new Vector<String>();
   
   public static int tablePrefixToInt(String prefix)
   {
      int num = 0;
      int multiplier = 1;
      for (int n = prefix.length() - 1; n >= 0; n--)
      {
         int offset = TableLetters.indexOf(prefix.substring(n, n+1));
         assert(offset != -1);
         if (n == prefix.length() - 1)
            num += offset * multiplier;
         else 
            num += (offset+1) * multiplier;
         multiplier *= TableLetters.length();
      }
      return num;
   }
   
   public static String intToTablePrefix(int num)
   {
      String prefix = "";
      num = num + 1;
      while (num > 0)
      {
         int offset = ((num - 1) % TableLetters.length());
         prefix = TableLetters.substring(offset, offset+1) + prefix;
         num = (num - 1) / TableLetters.length();
      }
      return prefix;
   }
   
   // Returns prefix for the new table added to the select..from..where
   public String addTable(String tableName)
   {
      String prefix = intToTablePrefix(nextTableNumber);
      nextTableNumber++;

      tables.add(tableName);
      
      return prefix;
   }

   
   // Returns a unique alias that can be used for naming new columns
   int nextAliasIndex = 0;
   public String nextAlias()
   {
      String alias = "COL" + nextAliasIndex;
      nextAliasIndex++;
      return alias;
   }
   
   
   // Stuff for handling SELECT
   static class Select
   {
      final QueryStringWithParameters column;
      final String alias;
      public Select(String column, String alias) { this.column = new QueryStringWithParameters(column, null); this.alias = alias; }
      public Select(String column, Object []parameters, String alias) { this.column = new QueryStringWithParameters(column, parameters); this.alias = alias; }
   }
   Vector<Select> selection = new Vector<Select>();

   public int addSelection(String column, Object[] params, String as)
   {
      int columnNumber = selection.size() + grouping.size();
      selection.add(new Select(column, params, as));
      return columnNumber + 1;
   }
   public int addSelection(String column, String as)
   {
      int columnNumber = selection.size() + grouping.size();
      selection.add(new Select(column, as));
      return columnNumber + 1;
   }

   // Stuff for handling GROUP BY (all things in the GROUP BY
   // will also appear in the SELECT)
   Vector<Select> grouping = new Vector<Select>();

   public boolean isGrouped()
   {
      return !grouping.isEmpty();
   }
   
   public int addGroupBy(String column, Object[] params, String as)
   {
      int columnNumber = selection.size() + grouping.size();
      grouping.add(new Select(column, params, as));
      return columnNumber + 1;
   }
   public int addGroupBy(String column, String as)
   {
      int columnNumber = selection.size() + grouping.size();
      grouping.add(new Select(column, as));
      return columnNumber + 1;
   }


   // Stuff for handling WHERE
   static class Clause
   {
      final String sql;
      final Object [] substitutions;
      public Clause(String sql, Object [] subs) {this.sql = sql; substitutions = subs; }
   }
   Vector<QueryStringWithParameters> whereClauses = new Vector<QueryStringWithParameters>();
   public void addWhereClause(String clause)
   {
      if (!clause.equals(""))
         whereClauses.add(new QueryStringWithParameters(clause, new Object[0]));
   }
   public void addWhereClause(String clause, Object obj1)
   {
      if (!clause.equals(""))
         whereClauses.add(new QueryStringWithParameters(clause, new Object[] {obj1}));
   }
   public void addWhereClause(String clause, Object obj1, Object obj2)
   {
      if (!clause.equals(""))
         whereClauses.add(new QueryStringWithParameters(clause, new Object[] {obj1, obj2}));
   }
   public void addWhereClause(String clause, Object []objs)
   {
      if (!clause.equals(""))
         whereClauses.add(new QueryStringWithParameters(clause, objs));
   }

   // Stuff for handling HAVING
   Vector<QueryStringWithParameters> havingClauses = new Vector<QueryStringWithParameters>();
   public void addHavingClause(String clause)
   {
      if (!clause.equals(""))
         havingClauses.add(new QueryStringWithParameters(clause, new Object[0]));
   }
   public void addHavingClause(String clause, Object obj1)
   {
      if (!clause.equals(""))
         havingClauses.add(new QueryStringWithParameters(clause, new Object[] {obj1}));
   }
   public void addHavingClause(String clause, Object obj1, Object obj2)
   {
      if (!clause.equals(""))
         havingClauses.add(new QueryStringWithParameters(clause, new Object[] {obj1, obj2}));
   }
   public void addHavingClause(String clause, Object []objs)
   {
      if (!clause.equals(""))
         havingClauses.add(new QueryStringWithParameters(clause, objs));
   }
   
   
   // Stuff for handling LIMIT
   int limit = 0;
   boolean isLimited = false;
   
   public void limit(int limit)
   {
      if (!isLimited || limit < this.limit)
         this.limit = limit;
      isLimited = true;
   }
   public boolean isLimited()
   {
      return isLimited;
   }
   
   // Stuff for handling ORDER BY
   static class OrderBy
   {
      public QueryStringWithParameters column;
      boolean isAscending;
      public OrderBy(QueryStringWithParameters qswp, boolean ascending) { column = qswp; isAscending = ascending; }
   }
   Vector<OrderBy> ordering = new Vector<OrderBy>();
   public void addOrdering(String column, Object[] parameters, boolean isAscending)
   {
      ordering.add(new OrderBy(new QueryStringWithParameters(column, parameters), isAscending));
   }
   
   public boolean isOrdered()
   {
      return !ordering.isEmpty();
   }
   
   
   public PreparedStatement makeQuery(Connection con) throws SQLException
   {
      PreparedStatement ps = null;

      String sql = "SELECT";
      if (selection.size() == 0 && grouping.size() == 0)
      {
         sql += " 1";
      }
      else
      {
         boolean isFirst = true;
         for (Select select : grouping)
         {
            if (isFirst)
               sql += " ";
            else
               sql += ", ";
            isFirst = false;
            sql += select.column.getQuery() + " AS " + select.alias;
         }
         for (Select select : selection)
         {
            if (isFirst)
               sql += " ";
            else
               sql += ", ";
            isFirst = false;
            sql += select.column.getQuery() + " AS " + select.alias;
         }
      }
      
      for (int n = 0; n < tables.size(); n++)
      {
         if (n == 0)
            sql += " FROM ";
         else
            sql += ", ";
         sql += tables.get(n) + " AS " + intToTablePrefix(n); 
      }

      for (int n = 0; n < whereClauses.size(); n++)
      {
         if (n == 0)
            sql += " WHERE ";
         else
            sql += " AND ";
         sql += whereClauses.get(n).getQuery();
      }

      if (!grouping.isEmpty())
      {
         sql += " GROUP BY ";
         boolean isFirst = true;
         for (Select select : grouping)
         {
            if (isFirst)
               sql += " ";
            else
               sql += ", ";
            isFirst = false;
            sql += select.column.getQuery();
         }
      }

      if (!havingClauses.isEmpty())
      {
         for (int n = 0; n < havingClauses.size(); n++)
         {
            if (n == 0)
               sql += " HAVING ";
            else
               sql += " AND ";
            sql += havingClauses.get(n).getQuery();
         }
      }
      
      if (!ordering.isEmpty())
      {
         sql += " ORDER BY ";
         for (int n = ordering.size() - 1; n >= 0; n--)
         {
            if (n != ordering.size() - 1)
               sql += ", ";
            sql += ordering.get(n).column.getQuery();
            if (!ordering.get(n).isAscending)
               sql += " DESC";
         }
      }
      
      ps = con.prepareStatement(sql);
      int idx = 1;
      for (Select select: grouping)
      {
         idx = select.column.setParameters(ps, idx);
      }
      for (Select select: selection)
      {
         idx = select.column.setParameters(ps, idx);
      }
      for (QueryStringWithParameters clause: whereClauses)
      {
         idx = clause.setParameters(ps, idx);
      }
      for (Select select: grouping)
      {
         idx = select.column.setParameters(ps, idx);
      }
      for (QueryStringWithParameters clause: havingClauses)
      {
         idx = clause.setParameters(ps, idx);
      }
      for (OrderBy order: ordering)
      {
         idx = order.column.setParameters(ps, idx);
      }

      if (isLimited)
      {
         ps.setMaxRows(limit);
      }
      
      return ps;
   }
   
   public Object clone() throws CloneNotSupportedException
   {
      SelectFromWhere clone = (SelectFromWhere)super.clone();
      clone.selection = (Vector<Select>) selection.clone();
      clone.tables = (Vector<String>) tables.clone();
      clone.whereClauses = (Vector<QueryStringWithParameters>) whereClauses.clone();
      clone.ordering = (Vector<OrderBy>) ordering.clone();
      clone.grouping = (Vector<Select>) grouping.clone();
      clone.havingClauses = (Vector<QueryStringWithParameters>) havingClauses.clone();
      return clone;
   }
   
   public SelectFromWhere copy()
   {
      SelectFromWhere toReturn = null;
      try
      {
         toReturn = (SelectFromWhere)clone();
      } catch (CloneNotSupportedException e)
      {
         e.printStackTrace();
      }
      return toReturn;
   }
}
