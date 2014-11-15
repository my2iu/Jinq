package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.RowReader;
import org.jinq.jpa.jpqlquery.ScalaTupleRowReader;

public class ScalaGroupingTransform extends GroupingTransform
{
   public ScalaGroupingTransform(JPQLQueryTransformConfiguration config)
   {
      super(config);
   }

   @Override 
   protected <U> RowReader<U> createTupleReader(RowReader<?>[] readers)
   {
      return ScalaTupleRowReader.createReaderForTuple(readers);
   }
   
   @Override 
   public String getTransformationTypeCachingTag()
   {
      return ScalaGroupingTransform.class.getName();
   }

}
