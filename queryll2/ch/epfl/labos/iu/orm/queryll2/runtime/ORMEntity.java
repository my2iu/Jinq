package ch.epfl.labos.iu.orm.queryll2.runtime;

public class ORMEntity
{
   public ORMEntity(String entityPackage, String name, String table, ORMField[] fields)
   {
      this.entityPackage = entityPackage;
      this.name = name;
      this.table = table;
      this.fields = fields;
   }
   public String entityPackage;
   public String name;
   public String table;
   public ORMField[] fields;
}
