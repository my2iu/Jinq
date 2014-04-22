package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.Expression;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitor;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class SymbExToExpression extends TypedValueVisitor<Void, Expression, TypedValueVisitorException>
{
   Expression transform(TypedValue val) throws TypedValueVisitorException
   {
      return val.visit(this, null);
   }

   @Override public Expression defaultValue(TypedValue val, Void in) throws TypedValueVisitorException
   {
      throw new TypedValueVisitorException("Unhandled symbolic execution operation: " + val);
   }

}
