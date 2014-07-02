package org.jinq.jpa.transform;

import org.jinq.jpa.MetamodelUtil;
import org.jinq.jpa.jpqlquery.BinaryExpression;
import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.ConstantExpression;
import org.jinq.jpa.jpqlquery.JPQLQuery;
import org.jinq.jpa.jpqlquery.ReadFieldExpression;
import org.jinq.jpa.jpqlquery.RowReader;
import org.jinq.jpa.jpqlquery.SimpleRowReader;
import org.jinq.jpa.jpqlquery.TupleRowReader;
import org.objectweb.asm.Type;

import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;
import ch.epfl.labos.iu.orm.queryll2.symbolic.ConstantValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitor;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class SymbExToSubQuery extends TypedValueVisitor<SymbExPassDown, JPQLQuery<?>, TypedValueVisitorException>
{
   final MetamodelUtil metamodel;
   final SymbExArgumentHandler argHandler;
   
   SymbExToSubQuery(MetamodelUtil metamodel, SymbExArgumentHandler argumentHandler)
   {
      this.metamodel = metamodel;
      this.argHandler = argumentHandler;
   }
   
   @Override public JPQLQuery<?> defaultValue(TypedValue val, SymbExPassDown in) throws TypedValueVisitorException
   {
      throw new TypedValueVisitorException("Unhandled symbolic execution operation: " + val);
   }

//   @Override public JPQLQuery<?> argValue(TypedValue.ArgValue val, SymbExPassDown in) throws TypedValueVisitorException
//   {
//      int index = val.getIndex();
//      return argHandler.handleArg(index, val.getType());
//   }


   @Override public JPQLQuery<?> virtualMethodCallValue(MethodCallValue.VirtualMethodCallValue val, SymbExPassDown in) throws TypedValueVisitorException
   {
      MethodSignature sig = val.getSignature();
      if (MetamodelUtil.inQueryStream.equals(sig))
      {
         if (!(val.base instanceof TypedValue.ArgValue))
            throw new TypedValueVisitorException("InQueryStreamSource comes from unknown source");
         int index = ((TypedValue.ArgValue)val.base).getIndex();
         if (!argHandler.checkIsInQueryStreamSource(index))
            throw new TypedValueVisitorException("InQueryStreamSource comes from unknown source");
         if (!(val.args.get(0) instanceof ConstantValue.ClassConstant))
            throw new TypedValueVisitorException("Streaming an unknown type");
         Type type = ((ConstantValue.ClassConstant)val.args.get(0)).val;
         String entityName = metamodel.entityNameFromClassName(type.getClassName());
         if (entityName == null)
            throw new TypedValueVisitorException("Streaming an unknown type");
         return JPQLQuery.findAllEntities(entityName);
      }
      else
         return super.virtualMethodCallValue(val, in);
   }

}
