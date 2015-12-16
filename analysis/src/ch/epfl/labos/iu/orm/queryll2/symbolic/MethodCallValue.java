/**
 * 
 */
package ch.epfl.labos.iu.orm.queryll2.symbolic;

import java.util.List;

import org.jinq.rebased.org.objectweb.asm.Type;

public class MethodCallValue extends TypedValue
{
   public String owner;  // class that owns the method
   public String name;   // name of the method
   public String desc;   // method parameters
   public List<TypedValue> args;
   public MethodCallValue(String owner, String name, String desc, List<TypedValue> args, Type returnType)
   {
      super(returnType);
      this.owner = owner;
      this.name = name;
      this.desc = desc;
      this.args = args;
   }

   public String toString()
   {
//      String str = owner + ":" + name + "(";
      String str = name + "(";
      boolean isFirst = true;
      for (TypedValue val: args)
      {
         if (!isFirst)
            str += ", ";
         isFirst = false;
         str += val.toString();
      }
      str += ")";
      return str;
   }
   
   @Override public <I,O,E extends Exception> O visit(TypedValueVisitor<I,O, E> visitor, I input) throws E
   {
      return visitor.methodCallValue(this, input);
   }

   public MethodCallValue withNewArgs(List<TypedValue> newArgs)
   {
      return new MethodCallValue(owner, name, desc, newArgs, type);
   }
   
   public MethodSignature getSignature()
   {
      return new MethodSignature(owner, name, desc);
   }
   
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((args == null) ? 0 : args.hashCode());
      result = prime * result + ((desc == null) ? 0 : desc.hashCode());
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((owner == null) ? 0 : owner.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (getClass() != obj.getClass())
         return false;
      MethodCallValue other = (MethodCallValue) obj;
      if (args == null)
      {
         if (other.args != null)
            return false;
      } else if (!args.equals(other.args))
         return false;
      if (desc == null)
      {
         if (other.desc != null)
            return false;
      } else if (!desc.equals(other.desc))
         return false;
      if (name == null)
      {
         if (other.name != null)
            return false;
      } else if (!name.equals(other.name))
         return false;
      if (owner == null)
      {
         if (other.owner != null)
            return false;
      } else if (!owner.equals(other.owner))
         return false;
      return true;
   }

   // For static and special calls
   public static class StaticMethodCallValue extends MethodCallValue
   {
      public StaticMethodCallValue(String owner, String name,
            String desc, List<TypedValue> args)
      {
         super(owner, name, desc, args, Type.getReturnType(desc));
      }
      @Override public <I,O,E extends Exception> O visit(TypedValueVisitor<I,O,E> visitor, I input) throws E
      {
         return visitor.staticMethodCallValue(this, input);
      }
      public StaticMethodCallValue withNewArgs(List<TypedValue> newArgs)
      {
         return new StaticMethodCallValue(owner, name, desc, newArgs);
      }
   }
   
   public static class VirtualMethodCallValue extends MethodCallValue
   {
      public TypedValue base;
      public VirtualMethodCallValue(String owner, String name, String desc, List<TypedValue> args, TypedValue base)
      {
         super(owner, name, desc, args, name.equals("<init>") ? Type.getObjectType(owner) : Type.getReturnType(desc));
         this.base = base;
      }

      public boolean isConstructor()
      {
         return name.equals("<init>");
      }
      
      public String toString()
      {
         return base.toString() + "." + super.toString();
      }
      @Override public <I,O,E extends Exception> O visit(TypedValueVisitor<I,O,E> visitor, I input) throws E
      {
         return visitor.virtualMethodCallValue(this, input);
      }
      public VirtualMethodCallValue withNewArgs(List<TypedValue> newArgs, TypedValue newBase)
      {
         return new VirtualMethodCallValue(owner, name, desc, newArgs, newBase);
      }

      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = super.hashCode();
         result = prime * result + ((base == null) ? 0 : base.hashCode());
         return result;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (!super.equals(obj))
            return false;
         if (getClass() != obj.getClass())
            return false;
         VirtualMethodCallValue other = (VirtualMethodCallValue) obj;
         if (base == null)
         {
            if (other.base != null)
               return false;
         } else if (!base.equals(other.base))
            return false;
         return true;
      }
   }
}