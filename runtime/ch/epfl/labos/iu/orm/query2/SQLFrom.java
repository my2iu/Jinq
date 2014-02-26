package ch.epfl.labos.iu.orm.query2;

import java.util.List;
import java.util.Map;

import ch.epfl.labos.iu.orm.query2.SQLFragment.SubFragment;


public abstract class SQLFrom implements SQLComponent
{
   String tableAlias;
   
   public void prepareQuery(JDBCQuerySetup setup)
      throws QueryGenerationException
   {
      tableAlias = setup.nextTableAlias();
   }

   public abstract JDBCFragment generateQuery(JDBCQuerySetup setup)
      throws QueryGenerationException;

   public abstract SQLFrom precopy(Map<Object, Object> remap);
   public abstract void postcopy(Map<Object, Object> remap);

   public static SQLFrom fromTable(String tableName)
   {
      return new SQLFromTable(tableName);
   }
   
   public static class SQLFromTable extends SQLFrom
   {
      String tableName;
      SQLFromTable(String tableName)
      {
         this.tableName = tableName;
      }
      public SQLFrom precopy(Map<Object, Object> remap)
      {
         if (remap.containsKey(this))  // This should never happen?
            return (SQLFrom)remap.get(this);
         SQLFrom toReturn = new SQLFromTable(tableName);
         remap.put(this, toReturn);
         return toReturn;
      }
      public void postcopy(Map<Object, Object> remap) {}
      public void storeParamLinks(int lambdaIndex, List<ParameterLocation> params) 
         throws QueryGenerationException
      {
      }
      public JDBCFragment generateQuery(JDBCQuerySetup setup)
         throws QueryGenerationException
      {
         return new JDBCFragment(tableName + " AS " + tableAlias);
      }
   }

}
