package org.jinq.jpa.jpqlquery;

/**
 * When generating a JPQL query string, we also need to store a list of generated 
 * parameters that will need to substituted into the query before the query can be
 * run. This class is the data structure holding info about these parameters.
 */
public class GeneratedQueryParameter
{
   public GeneratedQueryParameter(String paramName, int lambdaIndex, int argIndex)
   {
      this.paramName = paramName;
      this.lambdaIndex = lambdaIndex;
      this.argIndex = argIndex;
   }
   public String paramName;
   public int lambdaIndex;
   public int argIndex;
}
