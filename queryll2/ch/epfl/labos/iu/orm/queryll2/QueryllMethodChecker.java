package ch.epfl.labos.iu.orm.queryll2;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.Type;

import ch.epfl.labos.iu.orm.queryll2.path.Annotations;
import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisMethodChecker;
import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;
import ch.epfl.labos.iu.orm.queryll2.symbolic.BasicSymbolicInterpreter;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;

final class QueryllMethodChecker implements PathAnalysisMethodChecker<QueryllPathAnalysisSupplementalInfo>
{
   private final DBSetSourceChecker checkDBSets;
   private final Set<TypedValue> unresolvedJinqStreams;
   private final Set<Class<?>> safeMethodAnnotations;
   private final List<MethodSignature> transformConstructorsCalled;
   private final JinqStreamSourceChecker checkJinqStreams;
   private final ORMInformation entityInfo;
   private final Set<MethodSignature> safeMethods;
   private final Set<MethodSignature> safeStaticMethods;
   private final List<String> otherTransformClasses;
   private final Set<TypedValue> unresolvedDBSets;

   QueryllMethodChecker(DBSetSourceChecker checkDBSets,
         Set<TypedValue> unresolvedDBSets,
         JinqStreamSourceChecker checkJinqStreams,
         Set<TypedValue> unresolvedJinqStreams,
         List<MethodSignature> transformConstructorsCalled,
         ORMInformation entityInfo, Set<Class<?>> safeMethodAnnotations,
         Set<MethodSignature> safeMethods,
         Set<MethodSignature> safeStaticMethods,
         List<String> otherTransformClasses)
   {
      this.checkDBSets = checkDBSets;
      this.unresolvedJinqStreams = unresolvedJinqStreams;
      this.safeMethodAnnotations = safeMethodAnnotations;
      this.transformConstructorsCalled = transformConstructorsCalled;
      this.checkJinqStreams = checkJinqStreams;
      this.entityInfo = entityInfo;
      this.safeMethods = safeMethods;
      this.safeStaticMethods = safeStaticMethods;
      this.otherTransformClasses = otherTransformClasses;
      this.unresolvedDBSets = unresolvedDBSets;
   }

   /* (non-Javadoc)
    * @see ch.epfl.labos.iu.orm.queryll2.PathAnalysisMethodChecker#isStaticMethodSafe(ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature)
    */
   @Override
   public boolean isStaticMethodSafe(MethodSignature m)
      { return safeStaticMethods.contains(m); }

   /* (non-Javadoc)
    * @see ch.epfl.labos.iu.orm.queryll2.PathAnalysisMethodChecker#isMethodSafe(ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature, ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue, java.util.List)
    */
   @Override
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
                  if (!t.getInternalName().equals(TransformationClassAnalyzer.DBSET_CLASS)) continue;
                  args.get(n).visit(checkDBSets, unresolvedDBSets);
               }
            } catch (TypedValueVisitorException e)
            {
               return false;
            }
            return true;
         }
         else if (entityInfo.jinqStreamMethods.contains(m))
         {
            Type[] argTypes = Type.getArgumentTypes(m.desc);
            try {
               base.visit(checkJinqStreams, unresolvedJinqStreams);
               for (int n = 0; n < argTypes.length; n++)
               {
                  Type t = argTypes[n];
                  if (t.getSort() != Type.OBJECT) continue;
                  if (!t.getInternalName().equals(TransformationClassAnalyzer.JINQSTREAM_CLASS)) continue;
                  args.get(n).visit(checkJinqStreams, unresolvedJinqStreams);
               }
            } catch (TypedValueVisitorException e)
            {
               return false;
            }
            return true;
         }
         else if (safeMethods.contains(m))
         {
            return true;
         }
         else
         {
            // Use reflection to get info about the method (or would it be better
            // to do this through direct bytecode inspection?)
            try
            {
               Method reflectedMethod = Annotations.asmMethodSignatureToReflectionMethod(m);
               return Annotations.methodHasSomeAnnotations(reflectedMethod, safeMethodAnnotations);
            } catch (ClassNotFoundException|NoSuchMethodException e)
            {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
            return false; 
            
         }
      }

   /* (non-Javadoc)
    * @see ch.epfl.labos.iu.orm.queryll2.PathAnalysisMethodChecker#getSupplementalInfo()
    */
   @Override
   public QueryllPathAnalysisSupplementalInfo getSupplementalInfo()
   {
      QueryllPathAnalysisSupplementalInfo info = new QueryllPathAnalysisSupplementalInfo();
      info.transformConstructorsCalled = new HashSet<MethodSignature>();
      info.transformConstructorsCalled.addAll(transformConstructorsCalled);
      info.unresolvedDBSets = new HashSet<TypedValue>();
      info.unresolvedDBSets.addAll(unresolvedDBSets);
      info.unresolvedJinqStreams = new HashSet<TypedValue>();
      info.unresolvedJinqStreams.addAll(unresolvedJinqStreams);
      return info;
   }
}