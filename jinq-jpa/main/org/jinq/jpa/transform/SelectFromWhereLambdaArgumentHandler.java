package org.jinq.jpa.transform;

import java.util.function.Function;

import org.jinq.jpa.jpqlquery.ColumnExpressions;
import org.jinq.jpa.jpqlquery.SelectFromWhere;

public class SelectFromWhereLambdaArgumentHandler implements Function<Integer, ColumnExpressions<?>>
{
   SelectFromWhere<?> sfw;
   
   public SelectFromWhereLambdaArgumentHandler(SelectFromWhere<?> sfw)
   {
      this.sfw = sfw;
   }
   
   @Override
   public ColumnExpressions<?> apply(Integer t)
   {
/*
      if (java8LambdaParams != null && val.getIndex() < java8LambdaParams.getNumCapturedArgs())
      {
         // Currently, we only support parameters of a few small simple types.
         // We should also support more complex types (e.g. entities) and allow
         // fields/methods of those entities to be called in the query (code
         // motion will be used to push those field accesses or method calls
         // outside the query where they will be evaluated and then passed in
         // as a parameter)
         Type t = val.getType();
         if (!allowedQueryParameterTypes.containsKey(t))
            throw new TypedValueVisitorException("Accessing a field with unhandled type");
         
         try
         {
            // TODO: Careful here. ParameterLocation is relative to the base
            // lambda, but if we arrive here from inside a nested query, "this"
            // might refer to a lambda nested inside the base lambda. (Of course,
            // nested queries with parameters aren't currently supported, so it
            // doesn't matter.)
            ParameterLocation paramLoc = ParameterLocation.createJava8LambdaArgAccess(val.getIndex(), lambdaIndex);
            SQLColumnValues toReturn = new SQLColumnValues(allowedQueryParameterTypes.get(t));
            assert(toReturn.getNumColumns() == 1);
            toReturn.columns[0].add(new SQLSubstitution.ExternalParameterLink(paramLoc));
            return toReturn;
         } catch (Exception e)
         {
            throw new TypedValueVisitorException(e); 
         } 
      }
      else
*/
      // TODO: For JPQL queries, I don't think it's necessary to make a copy of the columns
      //    because I think JPQL lets you substitute the same parameter into multiple locations
      //    in a query (unlike JDBC), which means we don't need separate state for query fragments
      //    that appear multiple times in the query tree.
      return sfw.cols;
   }

}
