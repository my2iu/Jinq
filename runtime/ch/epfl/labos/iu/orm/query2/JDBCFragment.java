package ch.epfl.labos.iu.orm.query2;

import java.util.List;
import java.util.Vector;

public class JDBCFragment
{
   public String query = "";
   public List<JDBCParameter> params = new Vector<JDBCParameter>();
   public List<JDBCParameterLink> paramLinks = new Vector<JDBCParameterLink>();
   
   public JDBCFragment() {}
   public JDBCFragment(String str) {this.query = str;}
   
   public void add(String str)
   {
      query += str;
   }
   public void add(JDBCFragment fragment)
   {
      query += fragment.query;
      params.addAll(fragment.params);
      paramLinks.addAll(fragment.paramLinks);
   }
   public void add(JDBCParameter param)
   {
      query += "?";
      params.add(param);
   }
   public void add(JDBCParameterLink param)
   {
      query += "?";
      paramLinks.add(param);
   }
}
