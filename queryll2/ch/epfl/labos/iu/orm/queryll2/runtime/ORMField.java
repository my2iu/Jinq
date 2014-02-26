package ch.epfl.labos.iu.orm.queryll2.runtime;

public class ORMField
{
   public ORMField(String name, String type, String column, boolean isKey, boolean isDummy)
   {
      this.name = name;
      this.type = type;
      this.column = column;
      this.isKey = isKey;
      this.isDummy = isDummy;
   }
   public String name;
   public String type;
   public String column;
   public boolean isKey;
   public boolean isDummy;
}
