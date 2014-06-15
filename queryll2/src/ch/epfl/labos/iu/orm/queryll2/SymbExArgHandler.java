package ch.epfl.labos.iu.orm.queryll2;

import ch.epfl.labos.iu.orm.query2.SQLQuery;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public interface SymbExArgHandler<U>
{
   public abstract SQLColumnValues argValue(TypedValue.ArgValue val, U in) throws TypedValueVisitorException;
   public abstract SQLQuery argSubQueryValue(TypedValue.ArgValue val, U in) throws TypedValueVisitorException;
}