package ch.epfl.labos.iu.orm.query2;

import java.util.List;
import java.util.Map;
import java.util.Vector;

public class SQLFragment implements SQLComponent
{
   List<SubFragment> fragments = new Vector<SubFragment>();

   public SQLFragment() {}
   public SQLFragment(String str) {this(); add(str);}
   
   void addSubFragment(SubFragment toAdd)
   {
      if (!fragments.isEmpty())
      {
         // Combine subfragments that are just strings
         SubFragment last = fragments.get(fragments.size() - 1);
         if (last.str != null && last.sub == null && toAdd.str != null && toAdd.sub == null)
            fragments.set(fragments.size() - 1, new SubFragment(last.str + toAdd.str, null));
         else
            fragments.add(toAdd);
      }
      else
         fragments.add(toAdd);
   }
   public void add(String str)
   {
      addSubFragment(new SubFragment(str, null));
   }
   public void add(SQLSubstitution sub)
   {
      addSubFragment(new SubFragment(null, sub));
   }
   public void add(SQLFragment toAdd)
   {
      for (SubFragment fragment: toAdd.fragments)
         addSubFragment(fragment);
   }
   public boolean isEmpty()
   {
      return fragments.isEmpty();
   }
   
   @Override public int hashCode()
   {
      int code = 0;
      for (SubFragment fragment: fragments)
         code ^= fragment.hashCode();
      return code;
   }
   
   public boolean equals(Object o)
   {
      if (!(o instanceof SQLFragment)) return false;
      SQLFragment other = (SQLFragment)o;
      if (other.fragments.size() != fragments.size()) return false;
      for (int n = 0; n < fragments.size(); n++)
      {
         if (fragments.get(n).str == null)
         {
            if (other.fragments.get(n).str != null) return false;
         }
         else if (!fragments.get(n).str.equals(other.fragments.get(n).str))
            return false;
         if (fragments.get(n).sub == null)
         {
            if (other.fragments.get(n).sub != null) return false;
         }
         else if (!fragments.get(n).sub.equals(other.fragments.get(n).sub))
            return false;
      }
      return true;
   }
   
     
   public static class SubFragment
   {
      public String str;
      public SQLSubstitution sub;

      public SubFragment(String str, SQLSubstitution sub)
      {
         this.str = str;
         this.sub = sub;
      }
      public SubFragment precopy(Map<Object, Object> remap)
      {
         if (remap.containsKey(this))  // This should never happen?
            return (SubFragment)remap.get(this);
         SQLSubstitution newSub = null;
         if (sub != null) newSub = sub.precopy(remap);
         SubFragment toReturn = new SubFragment(str, newSub);
         remap.put(this, toReturn);
         return toReturn;
      }
      public void postcopy(Map<Object, Object> remap)
      {
         if (sub != null)
            sub.postcopy(remap);
      }
      @Override public int hashCode()
      {
         int code = 0;
         if (str != null) code ^= str.hashCode();
         if (sub != null) code ^= sub.hashCode();
         return code;
      }
   }

   public void storeParamLinks(int lambdaIndex, List<ParameterLocation> params) 
      throws QueryGenerationException
   {
      for (SubFragment sub: fragments)
         if (sub.sub != null)
            sub.sub.storeParamLinks(lambdaIndex, params);
   }
   
   public void prepareQuery(JDBCQuerySetup setup)
      throws QueryGenerationException
   {
      for (SubFragment sub: fragments)
         if (sub.sub != null)
            sub.sub.prepareQuery(setup);
   }
   public JDBCFragment generateQuery(JDBCQuerySetup setup)
         throws QueryGenerationException
   {
      JDBCFragment toReturn = new JDBCFragment();
      for (SubFragment sub: fragments)
      {
         if (sub.str != null)
            toReturn.add(sub.str);
         if (sub.sub != null)
            toReturn.add(sub.sub.generateQuery(setup));
      }
      return toReturn;
   }

   public SQLFragment precopy(Map<Object, Object> remap)
   {
      if (remap.containsKey(this))  // This should never happen?
         return (SQLFragment)remap.get(this);
      SQLFragment toReturn = new SQLFragment();
      remap.put(this, toReturn);
      for (SubFragment sub: fragments)
         toReturn.fragments.add(sub.precopy(remap));
      return toReturn;
   }
   public void postcopy(Map<Object, Object> remap)
   {
      for (SubFragment sub: fragments)
         sub.postcopy(remap);
   }
   public String toString() 
   {
      String toReturn = "";
      for (SubFragment sub: fragments)
         toReturn += sub.str;
      return toReturn;
   }
}
