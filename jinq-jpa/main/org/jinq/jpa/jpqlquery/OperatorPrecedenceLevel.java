package org.jinq.jpa.jpqlquery;

import java.util.HashMap;
import java.util.Map;

public class OperatorPrecedenceLevel
{
   String operator = "";
   int level = JPQL_UNKNOWN_PRECEDENCE_LEVEL;
   boolean isBelow = false;
   
   /**
    * Lookup table that maps an operator symbol to its precedence in
    * order of operations (lower numbers have higher precedence). This is
    * used to remove excess brackets, making queries easier to read and 
    * to not overstress certain JPQL implementations which can't handle 
    * the extra brackets sometimes.
    */
   public boolean hasPrecedence(OperatorPrecedenceLevel scope)
   {
      if (level == JPQL_UNKNOWN_PRECEDENCE_LEVEL) return false;
      if (scope.level == JPQL_UNKNOWN_PRECEDENCE_LEVEL) return false;
      return level <= scope.level;
   }
   
   public OperatorPrecedenceLevel getLevelBelow()
   {
      if (isBelow) throw new IllegalArgumentException("Tried to get two precedence levels below a level, which is not allowed");
      OperatorPrecedenceLevel precedence = new OperatorPrecedenceLevel();
      precedence.level = level - 1;
      precedence.operator = "below " + operator;
      precedence.isBelow = true;
      return precedence;
   }
   
   private static final int JPQL_UNKNOWN_PRECEDENCE_LEVEL = -1;
   private static final String JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE_STRING = "unrestricted";
   private static final String JPQL_ORDER_BY_UNRESTRICTED_OPERATOR_PRECEDENCE_STRING = "unrestricted for order by";
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
      JPQL_OPERATOR_PRECEDENCE.put("BETWEEN", 2160);
      JPQL_OPERATOR_PRECEDENCE.put("NOT BETWEEN", 2160);
      JPQL_OPERATOR_PRECEDENCE.put("LIKE", 2170);
      JPQL_OPERATOR_PRECEDENCE.put("NOT LIKE", 2170);
      JPQL_OPERATOR_PRECEDENCE.put("IN", 2180);
      JPQL_OPERATOR_PRECEDENCE.put("NOT IN", 2180);
      JPQL_OPERATOR_PRECEDENCE.put("IS NULL", 2190);
      JPQL_OPERATOR_PRECEDENCE.put("IS NOT NULL", 2190);
      JPQL_OPERATOR_PRECEDENCE.put("IS EMPTY", 2200);
      JPQL_OPERATOR_PRECEDENCE.put("IS NOT EMPTY", 2200);
      JPQL_OPERATOR_PRECEDENCE.put("MEMBER OF", 2210);
      JPQL_OPERATOR_PRECEDENCE.put("NOT MEMBER OF", 2210);

      JPQL_OPERATOR_PRECEDENCE.put("NOT", 3000);
      JPQL_OPERATOR_PRECEDENCE.put("AND", 3100);
      JPQL_OPERATOR_PRECEDENCE.put("OR", 3200);
      
      JPQL_OPERATOR_PRECEDENCE.put(JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE_STRING, 1000000);
      JPQL_OPERATOR_PRECEDENCE.put(JPQL_ORDER_BY_UNRESTRICTED_OPERATOR_PRECEDENCE_STRING, 1000010);
   }
   public static final OperatorPrecedenceLevel JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE = 
         OperatorPrecedenceLevel.forOperator(JPQL_UNRESTRICTED_OPERATOR_PRECEDENCE_STRING);
   public static final OperatorPrecedenceLevel JPQL_ORDER_BY_UNRESTRICTED_OPERATOR_PRECEDENCE = 
         OperatorPrecedenceLevel.forOperator(JPQL_ORDER_BY_UNRESTRICTED_OPERATOR_PRECEDENCE_STRING);
   public static final OperatorPrecedenceLevel JPQL_UNKNOWN_PRECEDENCE = 
         OperatorPrecedenceLevel.forOperator("");

   public static OperatorPrecedenceLevel forOperator(String op)
   {
      OperatorPrecedenceLevel level = new OperatorPrecedenceLevel();
      level.operator = op;
      if (JPQL_OPERATOR_PRECEDENCE.containsKey(op))
         level.level = JPQL_OPERATOR_PRECEDENCE.get(op);
      return level;
   }
}
