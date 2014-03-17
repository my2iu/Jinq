package ch.epfl.labos.iu.orm.queryll2;

import ch.epfl.labos.iu.orm.query2.EntityManagerBackdoor;
import ch.epfl.labos.iu.orm.query2.SQLQuery;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitor;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class SymbExToSubQueryGenerator<T> extends TypedValueVisitor<T, SQLQuery>
{
   ORMInformation entityInfo;
   SymbExLambdaContext<T> lambdaContext;
   QueryllSQLQueryTransformer queryMethodHandler;

   public SymbExToSubQueryGenerator(ORMInformation entityInfo, SymbExArgHandler<T> argHandler, SymbExGetFieldHandler<T> getFieldHandler, SymbExJoinHandler<T> joinHandler, QueryllSQLQueryTransformer queryMethodHandler) 
   {
      this.entityInfo = entityInfo;
      this.lambdaContext = new SymbExLambdaContext<T>(argHandler, getFieldHandler, joinHandler);
      this.queryMethodHandler = queryMethodHandler;
   }
   public SQLQuery generateFor(TypedValue val) throws TypedValueVisitorException
   {
      return val.visit(this, null);
   }

   @Override public SQLQuery argValue(TypedValue.ArgValue val, T in) throws TypedValueVisitorException
   {
      if (lambdaContext.args != null)
         return lambdaContext.args.argSubQueryValue(val, in);
      return super.argValue(val, in);
   }

   @Override public SQLQuery getFieldValue(TypedValue.GetFieldValue val, T in) throws TypedValueVisitorException
   {
      if (lambdaContext.fields != null)
         return lambdaContext.fields.getFieldSubQueryValue(val, in);
      return super.getFieldValue(val, in);
   }

   EntityManagerBackdoor getAndCheckEntityManager(TypedValue val)
   {
      if (lambdaContext.fields != null)
         return lambdaContext.fields.getAndCheckEntityManager(val);
      return null;
   }
   
   @Override public SQLQuery virtualMethodCallValue(MethodCallValue.VirtualMethodCallValue val, T in) throws TypedValueVisitorException
   {
      MethodSignature sig = val.getSignature();
      if (entityInfo.allEntityMethods.containsKey(sig))
      {
         String entityName = entityInfo.allEntityMethods.get(sig);
         EntityManagerBackdoor em = getAndCheckEntityManager(val.base);
         if (em == null)
            throw new TypedValueVisitorException("Accessing an unknown entity manager");
         return new SQLQuery.SelectFromWhere<T>(
               em.getReaderForEntity(entityName), 
               em.getEntityColumnNames(entityName),
               em.getTableForEntity(entityName));
      }
      else
         return super.virtualMethodCallValue(val, in);
   }
}
