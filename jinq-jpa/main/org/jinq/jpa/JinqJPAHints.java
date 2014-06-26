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
   
   public void setHint(String name, Object val)
   {
      if ("automaticPageSize".equals(name) && val instanceof Integer)
         automaticResultsPagingSize = (int)val;
      if ("queryLogger".equals(name) && val instanceof JPAQueryLogger)
         queryLogger = (JPAQueryLogger)val;
      if ("lambdaClassLoader".equals(name) && val instanceof ClassLoader)
         lambdaClassLoader = (ClassLoader)val;
      if ("exceptionOnTranslationFail".equals(name) && val instanceof Boolean)
         dieOnError = (Boolean)val;
   }
}
