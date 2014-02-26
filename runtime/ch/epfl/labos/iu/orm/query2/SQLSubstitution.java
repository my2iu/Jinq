package ch.epfl.labos.iu.orm.query2;

import java.util.List;
import java.util.Map;

public abstract class SQLSubstitution implements SQLComponent
{
   // Makes a copy of the object
   public abstract SQLSubstitution precopy(Map<Object, Object> remap);
   // Fixes references to other objects to point to the copies
   public abstract void postcopy(Map<Object, Object> remap);

   public void storeParamLinks(int lambdaIndex, List<ParameterLocation> params) 
      throws QueryGenerationException
   {
   }

   public void prepareQuery(JDBCQuerySetup setup)
         throws QueryGenerationException
   {
   }
   public abstract JDBCFragment generateQuery(JDBCQuerySetup setup)
         throws QueryGenerationException;
   
   public static class FromReference extends SQLSubstitution
   {
      SQLFrom from;
      public FromReference(SQLFrom from)
      {
         this.from = from;
      }
      public SQLSubstitution precopy(Map<Object, Object> remap)
      {
         if (remap.containsKey(this))  // This should never happen?
            return (SQLSubstitution)remap.get(this);
         SQLSubstitution toReturn = new FromReference(from);
         remap.put(this, toReturn);
         return toReturn;
      }
      public void postcopy(Map<Object, Object> remap)
      {
         if (remap.containsKey(from))
            from = (SQLFrom)remap.get(from);
      }
      public JDBCFragment generateQuery(JDBCQuerySetup setup)
         throws QueryGenerationException
      {
         return new JDBCFragment(from.tableAlias);
      }
      public int hashCode()
      {
         return from.hashCode();
      }
      public boolean equals(Object o)
      {
         if (!(o instanceof FromReference)) return false;
         FromReference other = (FromReference)o;
         return other.from == from;
      }
   }
   
   public static class ExternalParameterLink extends SQLSubstitution
   {
      ParameterLocation param;
      int paramIdx;
      
      public ExternalParameterLink(ParameterLocation param)
      {
         this.param = param;
         paramIdx = -1;
      }
      ExternalParameterLink(ParameterLocation param, int paramIdx)
      {
         this.param = param;
         this.paramIdx = paramIdx;
      }
      public SQLSubstitution precopy(Map<Object, Object> remap)
      {
         if (remap.containsKey(this))  // This should never happen?
            return (SQLSubstitution)remap.get(this);
         SQLSubstitution toReturn = new ExternalParameterLink(param, paramIdx);
         remap.put(this, toReturn);
         return toReturn;
      }
      public void postcopy(Map<Object, Object> remap) {}
      public void storeParamLinks(int lambdaIndex, List<ParameterLocation> params) 
         throws QueryGenerationException
      {
         if (param.getLambdaIndex() != lambdaIndex) return;
         assert(paramIdx == -1);
         int idx = params.indexOf(param);
         if (idx != -1)
         {
            paramIdx = idx;
            return;
         }
         paramIdx = params.size();
         params.add(param);
      }
      public JDBCFragment generateQuery(JDBCQuerySetup setup)
         throws QueryGenerationException
      {
         assert(paramIdx != -1);
         JDBCFragment fragment = new JDBCFragment();
         fragment.add(new JDBCParameterLink(param.getLambdaIndex(), paramIdx, param));
         return fragment;
      }
      public int hashCode()
      {
         return param.hashCode();
      }
      public boolean equals(Object o)
      {
         if (!(o instanceof ExternalParameterLink)) return false;
         ExternalParameterLink other = (ExternalParameterLink)o;
         return other.param.equals(param);  // TODO: Also check paramIdx for equality?
      }
   }
   
   // ExternalParameter is deprecated because we no longer put the parameter
   // directly into a query, but keep parameter values separate from the query
   public static @Deprecated class ExternalParameter extends SQLSubstitution
   {
      Object param;
      public ExternalParameter(Object param)
      {
         this.param = param;
      }
      public SQLSubstitution precopy(Map<Object, Object> remap)
      {
         if (remap.containsKey(this))  // This should never happen?
            return (SQLSubstitution)remap.get(this);
         SQLSubstitution toReturn = new ExternalParameter(param);
         remap.put(this, toReturn);
         return toReturn;
      }
      public void postcopy(Map<Object, Object> remap) {}
      public JDBCFragment generateQuery(JDBCQuerySetup setup)
         throws QueryGenerationException
      {
         JDBCFragment fragment = new JDBCFragment();
         fragment.add(new JDBCParameter(param));
         return fragment;
      }
      public int hashCode()
      {
         // This may not behave exactly as expected because two different
         // parameters will be considered equal and have the same hash code
         // even if they refer to different parameters. 
         return param.hashCode();
      }
      public boolean equals(Object o)
      {
         if (!(o instanceof ExternalParameter)) return false;
         ExternalParameter other = (ExternalParameter)o;
         return other.param.equals(param);
      }
   }
   
   public static class ScalarSelectFromWhereSubQuery extends SQLSubstitution
   {
      SQLQuery.SelectFromWhere sfw;
      public ScalarSelectFromWhereSubQuery(SQLQuery.SelectFromWhere sfw)
      {
         assert(sfw.reader.getNumColumns() == 1);
         this.sfw = sfw;
      }
      public SQLSubstitution precopy(Map<Object, Object> remap)
      {
         if (remap.containsKey(this))  // This should never happen?
            return (SQLSubstitution)remap.get(this);
         ScalarSelectFromWhereSubQuery toReturn = new ScalarSelectFromWhereSubQuery(sfw);
         remap.put(this, toReturn);
         toReturn.sfw = (SQLQuery.SelectFromWhere)sfw.precopy(remap);
         return toReturn;
      }
      public void postcopy(Map<Object, Object> remap)
      {
         sfw.postcopy(remap);
      }
      public void storeParamLinks(int lambdaIndex, List<ParameterLocation> params) 
         throws QueryGenerationException
      {
         sfw.storeParamLinks(lambdaIndex, params);
      }

      public void prepareQuery(JDBCQuerySetup setup)
         throws QueryGenerationException
      {
         sfw.prepareQuery(setup);
      }
      public JDBCFragment generateQuery(JDBCQuerySetup setup)
         throws QueryGenerationException
      {
         JDBCFragment fragment = new JDBCFragment();
         fragment.add("(");
         fragment.add(sfw.generateQuery(setup));
         fragment.add(")");
         return fragment;
      }
      public int hashCode()
      {
         return sfw.hashCode();
      }
      public boolean equals(Object o)
      {
         if (!(o instanceof ScalarSelectFromWhereSubQuery)) return false;
         ScalarSelectFromWhereSubQuery other = (ScalarSelectFromWhereSubQuery)o;
         return other.sfw.equals(sfw);
      }
   }
}
