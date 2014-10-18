package org.jinq.jpa;

class JinqJPAHints
{
   public JinqJPAHints() { }
   public JinqJPAHints(JinqJPAHints oldHints)
   {
      automaticResultsPagingSize = oldHints.automaticResultsPagingSize;
      queryLogger = oldHints.queryLogger;
      lambdaClassLoader = oldHints.lambdaClassLoader;
      dieOnError = oldHints.dieOnError;
   }
   
   public int automaticResultsPagingSize = 10000;
   public JPAQueryLogger queryLogger = null;
   public ClassLoader lambdaClassLoader = null;
   public boolean dieOnError = false;
   public boolean useCaching = true;
   
   public boolean setHint(String name, Object val)
   {
      if ("automaticPageSize".equals(name) && val instanceof Integer)
         automaticResultsPagingSize = (int)val;
      else if ("queryLogger".equals(name) && val instanceof JPAQueryLogger)
         queryLogger = (JPAQueryLogger)val;
      else if ("lambdaClassLoader".equals(name) && val instanceof ClassLoader)
         lambdaClassLoader = (ClassLoader)val;
      else if ("exceptionOnTranslationFail".equals(name) && val instanceof Boolean)
         dieOnError = (Boolean)val;
      else if ("useCaching".equals(name) && val instanceof Boolean)
         useCaching = (Boolean)val;
      else
         return false;
      return true;
   }
}
