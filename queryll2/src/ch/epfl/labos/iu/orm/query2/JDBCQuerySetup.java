package ch.epfl.labos.iu.orm.query2;

public class JDBCQuerySetup
{
   // For assigning table and column aliases to queries
   int nextCol = 1;
   int nextTable = 0;
   public String nextTableAlias()
   {
      int toReturn = nextTable;
      nextTable++;
      return intToTablePrefix(toReturn); 
   }
   public String nextColAlias()
   {
      int toReturn = nextCol;
      nextCol++;
      return "COL" + toReturn;
   }
   
   
   static final String TableLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
   
   public static int tablePrefixToInt(String prefix)
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
   
   public static String intToTablePrefix(int num)
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
