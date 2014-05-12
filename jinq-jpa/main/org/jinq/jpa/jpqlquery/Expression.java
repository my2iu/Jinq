package org.jinq.jpa.jpqlquery;

import java.util.HashMap;
import java.util.Map;

public class Expression
{
   public void generateQuery(QueryGenerationState queryState, String operatorPrecedenceScope)
   {
      
   }

   /**
    * Lookup table that maps an operator symbol to its precedence in
    * order of operations (lower numbers have higher precedence). This is
    * used to remove excess brackets, making queries easier to read and 
    * to not overstress certain JPQL implementations which can't handle 
    * the extra brackets sometimes.
    */
   public static boolean doesOperatorHaveJPQLPrecedence(String operator, String operatorScope)
   {
      if (!JPQL_OPERATOR_PRECEDENCE.containsKey(operator))
         return false;
      if (!JPQL_OPERATOR_PRECEDENCE.containsKey(operatorScope))
         return false;
      return JPQL_OPERATOR_PRECEDENCE.get(operator) <= JPQL_OPERATOR_PRECEDENCE.get(operatorScope);
   }
   public static final String JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE = "unrestricted";
   static final Map<String, Integer> JPQL_OPERATOR_PRECEDENCE = new HashMap<>();
   static {
      JPQL_OPERATOR_PRECEDENCE.put(".", 10);

      JPQL_OPERATOR_PRECEDENCE.put("+unary", 1000);
      JPQL_OPERATOR_PRECEDENCE.put("-unary", 1000);
      JPQL_OPERATOR_PRECEDENCE.put("*", 1100);
      JPQL_OPERATOR_PRECEDENCE.put("/", 1100);
      JPQL_OPERATOR_PRECEDENCE.put("+", 1200);
      JPQL_OPERATOR_PRECEDENCE.put("-", 1200);
      
      JPQL_OPERATOR_PRECEDENCE.put("=",  2100);
      JPQL_OPERATOR_PRECEDENCE.put(">",  2110);
      JPQL_OPERATOR_PRECEDENCE.put(">=", 2120);
      JPQL_OPERATOR_PRECEDENCE.put("<",  2130);
      JPQL_OPERATOR_PRECEDENCE.put("<=", 2140);
      JPQL_OPERATOR_PRECEDENCE.put("<>", 2150);

      JPQL_OPERATOR_PRECEDENCE.put("NOT", 3000);
      JPQL_OPERATOR_PRECEDENCE.put("AND", 3100);
      JPQL_OPERATOR_PRECEDENCE.put("OR", 3200);
      
      JPQL_OPERATOR_PRECEDENCE.put(JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE, 1000000);
   }         
}
