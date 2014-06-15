package ch.epfl.labos.iu.orm.queryll2.runtime;

public interface QueryllEntityConfigurationInfo
{
   // I can't work out all this XSLT stuff any more, so instead of
   // directly feeding in information about how to transform
   // methods into queries, I'll feed in the ORM mapping information 
   // directly and then decode that into translation information here
   // (which duplicates some of the work done in the XSLT ORM stuff,
   // but it's just getting too messy to modify that stuff)
   public void registerORMEntity(ORMEntity entity);
   public void registerORMSimpleLink(String entityPackage, String map, 
                                     String fromEntity, String fromField, String fromCol, 
                                     String toEntity, String toField, String toCol);
   public void registerORMNMLink(String entityPackage, String fromEntity, String fromField, String fromCol, 
                                 String linkTable, String linkInCol, String linkOutCol, 
                                 String toEntity, String toField, String toCol);
}
