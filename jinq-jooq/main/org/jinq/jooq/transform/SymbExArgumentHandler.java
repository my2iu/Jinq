package org.jinq.jooq.transform;

import org.jinq.jooq.querygen.ColumnExpressions;
import org.jinq.rebased.org.objectweb.asm.Type;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

// TODO: Creating a whole interface for handling arguments might be overkill. I'm not sure
//    how many variants there actually are

public interface SymbExArgumentHandler
{
   ColumnExpressions<?> handleArg(int argIndex, Type argType) throws TypedValueVisitorException;
}
