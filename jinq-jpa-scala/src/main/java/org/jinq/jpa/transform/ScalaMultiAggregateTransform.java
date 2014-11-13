package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.RowReader;
import org.jinq.jpa.jpqlquery.ScalaTupleRowReader;

public class ScalaMultiAggregateTransform extends MultiAggregateTransform
{

   public ScalaMultiAggregateTransform(JPQLQueryTransformConfiguration config)
   {
      super(config);
   }

   protected <U> RowReader<U> createTupleReader(RowReader<?>[] readers)
   {
      return ScalaTupleRowReader.createReaderForTuple(readers);
   }
   
   @Override 
   public String getTransformationTypeCachingTag()
   {
      return ScalaMultiAggregateTransform.class.getName();
   }

}
