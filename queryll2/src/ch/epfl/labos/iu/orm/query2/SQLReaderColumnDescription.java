package ch.epfl.labos.iu.orm.query2;

public class SQLReaderColumnDescription
{
   public SQLReaderColumnDescription(String field, String columnName, String type)
   {
      this.field = field;
      this.columnName = columnName;
      this.type = type;
   }
   public String field;
   public String columnName;
   public String type;
}
