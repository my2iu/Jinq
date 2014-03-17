package ch.epfl.labos.iu.orm.queryll2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import ch.epfl.labos.iu.orm.queryll2.symbolic.BasicSymbolicInterpreter;
import ch.epfl.labos.iu.orm.queryll2.symbolic.FrameWithHelpers;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.SymbolicInterpreterWithFieldAccess;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;


public class CodePath
{
   List<PathInstruction> path;
   public CodePath(List<PathInstruction> path)
   {
      this.path = new Vector<PathInstruction>(path);
   }
   
   // Path instructions contain a bytecode instruction, plus any annotations
   // that maybe needed
   
   public static class PathInstruction
   {
      PathInstruction(AbstractInsnNode node)
      {
         this.node = node;
      }
      AbstractInsnNode node;
      boolean isBranchTaken = false;
      boolean isBranch = false;
      static PathInstruction branch(AbstractInsnNode node, boolean branchTaken)
      {
         PathInstruction pi = new PathInstruction(node);
         pi.isBranch = true;
         pi.isBranchTaken = branchTaken;
         return pi;
      }
   }

   // Methods for constructing code paths from a method (assumes there are no loops) 
   
   // Returns false on encountering something that can't be analyzed properly
   static boolean breakIntoPaths(List<CodePath> paths, List<PathInstruction> previousInstructions, int index, CFG cfg, MethodNode m)
   {
      // Get the next instruction and check if we can handle it
      AbstractInsnNode instruction = m.instructions.get(index);
      
      // TODO: various control-flow instructions that we don't currently handle
      if (instruction instanceof LookupSwitchInsnNode) return false;
      if (instruction instanceof TableSwitchInsnNode) return false;
      if (instruction.getOpcode() == Opcodes.JSR) return false;
      if (instruction.getOpcode() == Opcodes.RET) return false;
      
      // Handle conditional branches specially
      if (instruction instanceof JumpInsnNode && instruction.getOpcode() != Opcodes.GOTO)
      {
         int nextIndex = index+1;
         int branchIndex = index+1;
         for (int next: cfg.succsOf(index))
            if (next != nextIndex)
               branchIndex = next;
         // Add the instruction to the path, recurse, then remove the instruction
         previousInstructions.add(PathInstruction.branch(instruction, true));
         if (!breakIntoPaths(paths, previousInstructions, branchIndex, cfg, m))
            return false;
         previousInstructions.remove(previousInstructions.size() - 1);

         // Add the instruction to the path, recurse, then remove the instruction
         previousInstructions.add(PathInstruction.branch(instruction, false));
         if (!breakIntoPaths(paths, previousInstructions, nextIndex, cfg, m))
            return false;
         previousInstructions.remove(previousInstructions.size() - 1);
      }
      else if (cfg.succsOf(index) != null)
      {
         // Add the instruction to the path, recurse, then remove the instruction
         assert(cfg.succsOf(index).size() == 1);
         int nextIndex = cfg.succsOf(index).get(0);
         previousInstructions.add(new PathInstruction(instruction));
         if (!breakIntoPaths(paths, previousInstructions, nextIndex, cfg, m))
            return false;
         previousInstructions.remove(previousInstructions.size() - 1);
      }
      else
      {
         // We've reached the end of a path, so record it
         previousInstructions.add(new PathInstruction(instruction));
         paths.add(new CodePath(previousInstructions));
         previousInstructions.remove(previousInstructions.size() - 1);
         
         // TODO: Abort if there are too many paths
      }
      
      return true;
   }
   static List<CodePath> breakIntoPaths(CFG cfg, MethodNode m, String name)
   {
      List<CodePath> paths = new Vector<CodePath>();
      List<PathInstruction> instructions = new Vector<PathInstruction>();
      if (!breakIntoPaths(paths, instructions, 0, cfg, m))
         return null;
      return paths;
   }
   public PathAnalysis calculateReturnValueAndConditions(
         ClassNode cl, MethodNode m,
         final Set<MethodSignature> safeMethods, 
         final Set<MethodSignature> safeStaticMethods,
         final List<String> otherTransformClasses,
         final ORMInformation entityInfo) throws AnalyzerException
   {
      class ConditionRecorder implements BasicSymbolicInterpreter.BranchHandler
      {
         List<TypedValue.ComparisonValue> conditions = new Vector<TypedValue.ComparisonValue>();
         boolean isBranchTaken = false;
         public void ifInstruction(AbstractInsnNode insn,
                                   TypedValue.ComparisonValue ifTrueValue)
         {
            if (isBranchTaken)
               conditions.add(ifTrueValue);
            else
               conditions.add(ifTrueValue.inverseValue());
         }
      }
      final List<MethodSignature> transformConstructorsCalled = new ArrayList<MethodSignature>();
      final DBSetSourceChecker checkDBSets = new DBSetSourceChecker(entityInfo);
      final Set<TypedValue> unresolvedDBSets = new HashSet<TypedValue>();
      ConditionRecorder pathConditions = new ConditionRecorder();
      BasicSymbolicInterpreter interpreter = new SymbolicInterpreterWithFieldAccess(Opcodes.ASM5);
      FrameWithHelpers frame = new FrameWithHelpers(cl, m, interpreter);
      interpreter.setFrameForAliasingFixups(frame);
      interpreter.setBranchHandler(pathConditions);
      interpreter.setMethodChecker(new BasicSymbolicInterpreter.MethodChecker() {
            public boolean isStaticMethodSafe(MethodSignature m)
               { return safeStaticMethods.contains(m); }
            public boolean isMethodSafe(MethodSignature m, TypedValue base, List<TypedValue> args)
               {
                  if (m.name.equals("<init>") && otherTransformClasses.contains(m.owner))
                  {
                     transformConstructorsCalled.add(m);
                     return true;
                  }
                  else if (entityInfo.dbSetMethods.contains(m))
                  {
                     Type[] argTypes = Type.getArgumentTypes(m.desc);
                     try {
                        base.visit(checkDBSets, unresolvedDBSets);
                        for (int n = 0; n < argTypes.length; n++)
                        {
                           Type t = argTypes[n];
                           if (t.getSort() != Type.OBJECT) continue;
                           if (!t.getInternalName().equals("Lch/epfl/labos/iu/orm/DBSet;")) continue;
                           args.get(n).visit(checkDBSets, unresolvedDBSets);
                        }
                     } catch (TypedValueVisitorException e)
                     {
                        return false;
                     }
                     return true;
                  }
                  else
                     return safeMethods.contains(m); 
               }});
      
      for (PathInstruction instruction: path)
      {
         // Skip "fake" instructions like Frame, LineNumber, and Label
         if (instruction.node.getOpcode() < 0) continue;
         
         if (instruction.isBranch)
            pathConditions.isBranchTaken = instruction.isBranchTaken;
         frame.execute(instruction.node, interpreter);
      }
      
      TypedValue returnValue = interpreter.returnValue;
      List<TypedValue.ComparisonValue> conditions = pathConditions.conditions;
      
      return new PathAnalysis(returnValue, conditions, transformConstructorsCalled, unresolvedDBSets);
   }

}
