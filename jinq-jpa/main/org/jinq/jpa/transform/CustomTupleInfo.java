package org.jinq.jpa.transform;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;

public class CustomTupleInfo
{
   public String className;
   public Method staticBuilder;
   public MethodSignature staticBuilderSig;
   public Constructor constructor;
   public MethodSignature constructorSig;
}
