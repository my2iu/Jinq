package org.jinq.jpa;

import java.util.regex.Pattern;

public class JPQL
{
   // In-memory implementation of JPQL like.
   public static boolean like(String str, String pattern)
   {
      return like(str, pattern, "");
   }
   
   // In-memory implementation of JPQL like.
   public static boolean like(String str, String pattern, String escapeChar)
   {
      String regex = "";
      String subpattern = "";
      while (!pattern.isEmpty())
      {
         // Ignore Unicode codepoint issues
         String nextChar = pattern.substring(0, 1);
         if (escapeChar.equals(nextChar))
         {
            if (pattern.length() > 1)
            {
               pattern = pattern.substring(1);
               String nextNextChar = pattern.substring(0, 1);
               subpattern += nextNextChar;
            }
         }
         else if ("_".equals(nextChar))
         {
            regex += Pattern.quote(subpattern);
            regex += ".";
            subpattern = "";
         }
         else if ("%".equals(nextChar))
         {
            regex += Pattern.quote(subpattern);
            regex += ".*";
            subpattern = "";
         }
         else
         {
            subpattern += nextChar;
         }
         pattern = pattern.substring(1);
      }
      regex += Pattern.quote(subpattern);
      return str.matches(regex);
   }
}
