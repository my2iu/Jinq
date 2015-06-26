package ch.epfl.labos.iu.orm.queryll2.symbolic;

import org.jinq.rebased.org.objectweb.asm.Opcodes;
import org.jinq.rebased.org.objectweb.asm.Type;
import org.jinq.rebased.org.objectweb.asm.tree.ClassNode;
import org.jinq.rebased.org.objectweb.asm.tree.MethodNode;
import org.jinq.rebased.org.objectweb.asm.tree.analysis.Frame;
import org.jinq.rebased.org.objectweb.asm.tree.analysis.Value;

// A version of ASM's Frame with some helper methods.

// A lot of this code is adapted from ASM's
//    org.objectweb.asm.tree.analysis.Analyzer
//       author Eric Bruneton
/*
* Copyright (c) 2000-2007 INRIA, France Telecom
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in the
*    documentation and/or other materials provided with the distribution.
* 3. Neither the name of the copyright holders nor the names of its
*    contributors may be used to endorse or promote products derived from
*    this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
* THE POSSIBILITY OF SUCH DAMAGE.
*/

public class FrameWithHelpers extends Frame
{
   // Configures a frame with this configured and method arguments set
   public FrameWithHelpers(ClassNode cl, MethodNode m, InterpreterWithArgs interpreter)
   {
      super(m.maxLocals, m.maxStack);
//      current.setReturn(interpreter.newValue(Type.getReturnType(m.desc)));
      Type[] args = Type.getArgumentTypes(m.desc);
      int local = 0;
      if ((m.access & Opcodes.ACC_STATIC) == 0) {
          Type ctype = Type.getObjectType(cl.name);
          setLocal(local++, interpreter.newThis(ctype));
      }
      for (int i = 0; i < args.length; ++i) {
          setLocal(local++, interpreter.newArg(args[i], i));
          if (args[i].getSize() == 2) {
              setLocal(local++, interpreter.newValue(null));
          }
      }
      while (local < m.maxLocals) {
          setLocal(local++, interpreter.newValue(null));
      }
//      merge(0, current, null);
   }
   
   // There may be aliased values in the stack. This helper method
   // scans through the stack and updates aliased values to new values.
   // Since this is used for aliasing of reference types, you shouldn't
   // do a replace on primitive values, so be sure to check for this
   // before calling this method.
   //
   public void replaceValues(Value oldVal, Value newVal)
   {
      if (oldVal instanceof TypedValue)
         if (((TypedValue)oldVal).isPrimitive()) return;
      
      // Change all locals to new value
      for (int n = 0; n < getLocals(); n++)
         if (getLocal(n).equals(oldVal))
            setLocal(n, newVal);
      
      // Pop out the contents of the stack, change them, then push them back
      Value [] stack = new Value[getStackSize()];
      int top = 0;
      while (getStackSize() > 0)
      {
         stack[top] = pop();
         if (stack[top].equals(oldVal))
            stack[top] = newVal;
         top++;
      }
      while(top > 0)
      {
         push(stack[top-1]);
         top--;
      }
   }
}
