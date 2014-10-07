package org.jinq.jpa.transform;

import javax.persistence.metamodel.Attribute;

/**
 * Holds information about JPA Criteria API Metamodel attribute. 
 * We don't need all the information from the metamodel, so it's 
 * easier to have a class holding only the fields we need.
 */
public class MetamodelUtilAttribute
{
   private String name;
   private boolean isAssociation;
   
   public MetamodelUtilAttribute(Attribute<?,?> singularAttrib)
   {
      this(singularAttrib.getName(), singularAttrib.isAssociation());
   }
   
   public MetamodelUtilAttribute(String name, boolean isAssociation)
   {
      this.name = name;
      this.isAssociation = isAssociation;
   }

   public String getName()
   {
      return name;
   }
   
   public boolean isAssociation()
   {
      return isAssociation;
   }
}
