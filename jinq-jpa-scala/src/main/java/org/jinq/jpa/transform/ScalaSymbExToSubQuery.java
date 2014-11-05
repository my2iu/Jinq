package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.objectweb.asm.Type;

import ch.epfl.labos.iu.orm.queryll2.symbolic.ConstantValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.LambdaFactory;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class ScalaSymbExToSubQuery extends SymbExToSubQuery
{
   ScalaSymbExToSubQuery(JPQLQueryTransformConfiguration config,
         SymbExArgumentHandler argumentHandler)
   {
      super(config, argumentHandler);
   }

   @Override public JPQLQuery<?> virtualMethodCallValue(MethodCallValue.VirtualMethodCallValue val, SymbExPassDown in) throws TypedValueVisitorException
   {
      MethodSignature sig = val.getSignature();
      if (ScalaMetamodelUtil.INQUERYSTREAMSOURCE_STREAM.equals(sig))
      {
         return handleInQueryStreamSource(val.base, val.args.get(0));
      }
      else
         return super.virtualMethodCallValue(val, in);
   }
   
}
