package ch.epfl.labos.iu.orm.queryll2;

import ch.epfl.labos.iu.orm.query2.EntityManagerBackdoor;
import ch.epfl.labos.iu.orm.query2.SQLQuery;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue.ArgValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue.GetFieldValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class SymbExLambdaContext<T> implements SymbExArgHandler<T>, SymbExGetFieldHandler<T>
{
   public SymbExArgHandler<T> args;
   public SymbExGetFieldHandler<T> fields;
   public SymbExJoinHandler<T> joins;
   public SymbExLambdaContext(SymbExArgHandler<T> args, SymbExGetFieldHandler<T> fields, SymbExJoinHandler<T> joins)
   {
      this.args = args;
      this.fields = fields;
      this.joins = joins;
   }

   public SQLColumnValues getFieldValue(GetFieldValue val, T in)
         throws TypedValueVisitorException
   {
      return fields.getFieldValue(val, in);
   }

   public EntityManagerBackdoor getAndCheckEntityManager(TypedValue val)
   {
      return fields.getAndCheckEntityManager(val);
   }

   public SQLQuery getFieldSubQueryValue(GetFieldValue val, T in)
         throws TypedValueVisitorException
   {
      return fields.getFieldSubQueryValue(val, in);
   }

   public SQLColumnValues argValue(ArgValue val, T in)
         throws TypedValueVisitorException
   {
      return args.argValue(val, in);
   }

   public SQLQuery argSubQueryValue(ArgValue val, T in)
         throws TypedValueVisitorException
   {
      return args.argSubQueryValue(val, in);
   }

}
