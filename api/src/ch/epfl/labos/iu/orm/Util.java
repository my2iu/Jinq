package ch.epfl.labos.iu.orm;

import java.sql.Date;

public class Util
{
   public static boolean SQLStringLike(String a, String b)
   {
      // TODO: Implement SQL Pattern matching
      return false;
   }

   public static String SQLSubstring(String a, int pos, int len)
   {
      int maxlen = a.length();
      if (pos == 0)
         return "";
      else if (pos < 0)
         return a.substring(maxlen + pos, Math.min(maxlen, maxlen + pos + len));
      else
         return a.substring(pos-1, Math.min(maxlen, pos-1+len));
   }

   public static String SQLSubstring(String a, int pos)
   {
      if (pos == 0)
         return "";
      else if (pos < 0)
      {
         int maxlen = a.length();
         return a.substring(maxlen + pos);
      }
      else
         return a.substring(pos-1);
   }

   public static Date addDays(Date date, int days)
   {
      // TODO: Don't bother with a proper correction here, since it'll all be
      // taken care of in the SQL
      return new Date(date.getTime() + 1000 * 60 * 60 * 24 * days);
   }
   public static Date addMonths(Date date, int months)
   {
      // TODO: Don't bother with a proper correction here, since it'll all be
      // taken care of in the SQL
      return addDays(date, 30 * months);
   }
   public static Date addYears(Date date, int years)
   {
      // TODO: Don't bother with a proper correction here, since it'll all be
      // taken care of in the SQL
      return addYears(date, 12 * years);
   }
   
   public static Date subDays(Date date, int days)
   {
      return addDays(date, -days);
   }
   public static Date subMonths(Date date, int months)
   {
      return addMonths(date, -months);
   }
   public static Date subYears(Date date, int years)
   {
      return addYears(date, -years);
   }
   
   public static int extractYear(Date date)
   {
      return date.getYear() + 1900;
   }
  
}
