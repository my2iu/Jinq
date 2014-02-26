package ch.epfl.labos.iu.orm.queryll2;

import ch.epfl.labos.iu.orm.query2.EntityManagerBackdoor;
import ch.epfl.labos.iu.orm.query2.SQLQuery;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public interface SymbExGetFieldHandler<U>
{
   public abstract SQLColumnValues getFieldValue(TypedValue.GetFieldValue val, U in) throws TypedValueVisitorException;
   public abstract EntityManagerBackdoor getAndCheckEntityManager(TypedValue val);
   public SQLQuery getFieldSubQueryValue(TypedValue.GetFieldValue val, U in) throws TypedValueVisitorException;

}
