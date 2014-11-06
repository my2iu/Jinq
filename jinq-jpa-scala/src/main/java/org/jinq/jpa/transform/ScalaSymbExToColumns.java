package org.jinq.jpa.transform;

import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.RowReader;
import org.jinq.jpa.jpqlquery.ScalaTupleRowReader;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodCallValue.StaticMethodCallValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue.GetFieldValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

public class ScalaSymbExToColumns extends SymbExToColumns
{
   ScalaSymbExToColumns(JPQLQueryTransformConfiguration config,
         SymbExArgumentHandler argumentHandler)
   {
      super(config, argumentHandler);
   }

   @Override public ColumnExpressions<?> virtualMethodCallValue(MethodCallValue.VirtualMethodCallValue val, SymbExPassDown in) throws TypedValueVisitorException
   {
      MethodSignature sig = val.getSignature();
      if (ScalaMetamodelUtil.newTuple2.equals(sig)
            || ScalaMetamodelUtil.newTuple3.equals(sig)
            || ScalaMetamodelUtil.newTuple4.equals(sig)
            || ScalaMetamodelUtil.newTuple5.equals(sig)
            || ScalaMetamodelUtil.newTuple8.equals(sig))
      {
         ColumnExpressions<?> [] vals = new ColumnExpressions<?> [val.args.size()];
         // TODO: This is a little wonky passing down isExpectingConditional, but I think it's right for those times you create a tuple with booleans and then extract the booleans later
         SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
         for (int n = 0; n < vals.length; n++)
            vals[n] = val.args.get(n).visit(this, passdown);
         RowReader<?> [] valReaders = new RowReader[vals.length];
         for (int n = 0; n < vals.length; n++)
            valReaders[n] = vals[n].reader;

         ColumnExpressions<?> toReturn = new ColumnExpressions<>(ScalaTupleRowReader.createReaderForTuple(sig.owner, valReaders));
         for (int n = 0; n < vals.length; n++)
            toReturn.columns.addAll(vals[n].columns);
         return toReturn;
      }
      else if (ScalaMetamodelUtil.TUPLE_ACCESSORS.containsKey(sig))
      {
         int idx = ScalaMetamodelUtil.TUPLE_ACCESSORS.get(sig) - 1;
         // TODO: This is a little wonky passing down isExpectingConditional, but I think it's right for those times you create a tuple with booleans and then extract the booleans later
         SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
         ColumnExpressions<?> base = val.base.visit(this, passdown);
         RowReader<?> subreader = ((ScalaTupleRowReader<?>)base.reader).getReaderForIndex(idx);
         ColumnExpressions<?> toReturn = new ColumnExpressions<>(subreader);
         int baseOffset = ((ScalaTupleRowReader<?>)base.reader).getColumnForIndex(idx);
         for (int n = 0; n < subreader.getNumColumns(); n++)
            toReturn.columns.add(base.columns.get(n + baseOffset));
         return toReturn;
      }
      else
         return super.virtualMethodCallValue(val, in);
   }

   @Override
   public ColumnExpressions<?> staticMethodCallValue(StaticMethodCallValue val,
         SymbExPassDown in) throws TypedValueVisitorException
   {
      MethodSignature sig = val.getSignature();
      if (sig.equals(ScalaMetamodelUtil.BOX_TO_INTEGER))
      {
         SymbExPassDown passdown = SymbExPassDown.with(val, in.isExpectingConditional);
         ColumnExpressions<?> base = val.args.get(0).visit(this, passdown);
         return base;
      }
      return super.staticMethodCallValue(val, in);
   }
   
   @Override
   public ColumnExpressions<?> getFieldValue(GetFieldValue val,
         SymbExPassDown in) throws TypedValueVisitorException
   {
      if (val.operand instanceof TypedValue.ThisValue)
      {
         return argHandler.handleThisFieldRead(val.name, val.getType());
      }
      return super.getFieldValue(val, in);
   }
}
