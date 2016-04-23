package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.RowReader;
import org.jinq.jpa.jpqlquery.ScalaTupleRowReader;

public class ScalaOuterJoinOnTransform extends OuterJoinOnTransform
{
   public ScalaOuterJoinOnTransform(JPQLQueryTransformConfiguration config)
   {
      super(config, true, false);
   }
   
   @Override
   protected <U> RowReader<U> createPairReader(RowReader<?> a, RowReader<?> b)
   {
      return ScalaTupleRowReader.createReaderForTuple(ScalaTupleRowReader.TUPLE2_CLASS, a, b);
   }
   
   @Override 
   public String getTransformationTypeCachingTag()
   {
      return ScalaOuterJoinOnTransform.class.getName();
   }
}
