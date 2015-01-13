package ch.epfl.labos.iu.orm.queryll2.symbolic;

import java.lang.reflect.Method;

import org.objectweb.asm.Type;

public class MethodSignature
{
   public MethodSignature(String owner, String name, String desc)
   {
      this.owner = owner;
      this.name = name;
      this.desc = desc;
   }
   
   public String owner;
   public String name;
   public String desc;
   
   public Type getOwnerType()
   {
      return Type.getObjectType(owner);
   }
   public Type getReturnType()
   {
      return Type.getReturnType(desc);
   }
   
   public @Override boolean equals(Object o)
   {
      if (!(o instanceof MethodSignature)) return false;
      MethodSignature m = (MethodSignature)o;
      return m.desc.equals(desc) && m.name.equals(name) && m.owner.equals(owner);
   }
   
   public boolean equals(String owner, String name, String desc)
   {
      return equals(new MethodSignature(owner, name, desc));
   }
   
   public @Override int hashCode()
   {
      return owner.hashCode() ^ name.hashCode() ^ desc.hashCode();
   }
   
   public @Override String toString()
   {
      return owner + ":" + name + desc;
   }
   
   public static MethodSignature fromMethod(Method m)
   {
      return new MethodSignature(
            Type.getInternalName(m.getDeclaringClass()), 
            m.getName(),
            Type.getMethodDescriptor(m));
   }
}
