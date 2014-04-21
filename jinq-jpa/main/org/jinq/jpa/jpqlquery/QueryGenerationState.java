package org.jinq.jpa.jpqlquery;

import java.util.IdentityHashMap;
import java.util.Map;

class QueryGenerationState
{
   String queryString = "";
   Map<From, String> fromAliases = new IdentityHashMap<>();
   
   /**
    * Gives a text label that can be used to identify an entry in the FROM section
    * of a JPQL query.
    * 
    * @param from 
    * @return a text label
    */
   public String generateFromAlias(From from)
   {
      String alias = nextTableAlias();
      fromAliases.put(from, alias);
      return alias;
   }

   /**
    * Returns the already generated alias for a given FROM entry.
    * @param from
    * @return
    */
   public String getFromAlias(From from)
   {
      return fromAliases.get(from);
   }
   
   /**
    * Adds some text to the current part of the query string being assembled.
    * @param str
    */
   public void appendQuery(String str)
   {
      // TODO: Perhaps this shouldn't be in QueryGenerationState but in a separate object just for holding query output.
      queryString += str;
   }
   
   // For assigning from and column aliases to queries
   int nextCol = 1;
   int nextTable = 0;
   private String nextTableAlias()
   {
      int toReturn = nextTable;
      nextTable++;
      return intToTablePrefix(toReturn); 
   }
   private String nextColAlias()
   {
      int toReturn = nextCol;
      nextCol++;
      return "COL" + toReturn;
   }
   
   
   static final String TableLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
   
   static int tablePrefixToInt(String prefix)
   {
      int num = 0;
      int multiplier = 1;
      for (int n = prefix.length() - 1; n >= 0; n--)
      {
         int offset = TableLetters.indexOf(prefix.substring(n, n+1));
         assert(offset != -1);
         if (n == prefix.length() - 1)
            num += offset * multiplier;
         else 
            num += (offset+1) * multiplier;
         multiplier *= TableLetters.length();
      }
      return num;
   }
   
   static String intToTablePrefix(int num)
   {
      String prefix = "";
      num = num + 1;
      while (num > 0)
      {
         int offset = ((num - 1) % TableLetters.length());
         prefix = TableLetters.substring(offset, offset+1) + prefix;
         num = (num - 1) / TableLetters.length();
      }
      return prefix;
   }
}
