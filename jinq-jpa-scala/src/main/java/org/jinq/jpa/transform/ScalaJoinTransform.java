package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.RowReader;
import org.jinq.jpa.jpqlquery.ScalaTupleRowReader;

public class ScalaJoinTransform extends JoinTransform
{
   public ScalaJoinTransform(JPQLQueryTransformConfiguration config, boolean withSource, boolean joinAsPairs)
   {
      super(config, withSource, joinAsPairs, true, false);
   }
   
   public ScalaJoinTransform(JPQLQueryTransformConfiguration config)
   {
      this(config, false, true);
   }

   @Override
   protected <U> RowReader<U> createPairReader(RowReader<?> a, RowReader<?> b)
   {
      return ScalaTupleRowReader.createReaderForTuple(ScalaTupleRowReader.TUPLE2_CLASS, a, b);
   }
   
   @Override 
   public String getTransformationTypeCachingTag()
   {
      return ScalaJoinTransform.class.getName();
   }
}
