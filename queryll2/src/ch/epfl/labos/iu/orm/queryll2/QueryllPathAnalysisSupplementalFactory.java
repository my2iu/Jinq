package ch.epfl.labos.iu.orm.queryll2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.ClassReader;

import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisMethodChecker;
import ch.epfl.labos.iu.orm.queryll2.path.PathAnalysisSupplementalFactory;
import ch.epfl.labos.iu.orm.queryll2.path.TransformationClassAnalyzer;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue.ComparisonValue;

public class QueryllPathAnalysisSupplementalFactory implements
      PathAnalysisSupplementalFactory<QueryllPathAnalysisSupplementalInfo, MethodAnalysisResults>
{
   private final ORMInformation entityInfo;
   private final Set<Class<?>> SafeMethodAnnotations;
   private final Set<MethodSignature> safeMethods;
   private final Set<MethodSignature> safeStaticMethods;
   private final List<String> allTransformClasses;

   public QueryllPathAnalysisSupplementalFactory(ORMInformation entityInfo, List<String> otherTransformClasses)
   {
      // Build up data structures and other information needed for analysis
      SafeMethodAnnotations = TransformationClassAnalyzer.SafeMethodAnnotations;
      safeMethods = new HashSet<MethodSignature>();
      safeMethods.addAll(TransformationClassAnalyzer.KnownSafeMethods);
      safeMethods.addAll(entityInfo.sideEffectFreeMethods);
      safeMethods.addAll(entityInfo.passThroughMethods);
      safeStaticMethods = new HashSet<MethodSignature>();
      safeStaticMethods.addAll(TransformationClassAnalyzer.KnownSafeStaticMethods);
      safeStaticMethods.addAll(entityInfo.sideEffectFreeStaticMethods);
      safeStaticMethods.addAll(entityInfo.passThroughStaticMethods);
      this.entityInfo = entityInfo;
      this.allTransformClasses = otherTransformClasses;
   }
   
   @Override
   public PathAnalysisMethodChecker createMethodChecker()
   {
      final DBSetSourceChecker checkDBSets = new DBSetSourceChecker(entityInfo);
      final Set<TypedValue> unresolvedDBSets = new HashSet<TypedValue>();
      // TODO: make sure that jinq streams are handled in the same way that dbsets are handled above
      final JinqStreamSourceChecker checkJinqStreams = new JinqStreamSourceChecker(entityInfo);
      final Set<TypedValue> unresolvedJinqStreams = new HashSet<TypedValue>();
      final List<MethodSignature> transformConstructorsCalled = new ArrayList<MethodSignature>();
      return new QueryllMethodChecker(checkDBSets, unresolvedDBSets, 
            checkJinqStreams, unresolvedJinqStreams,
            transformConstructorsCalled,
            entityInfo, SafeMethodAnnotations, 
            safeMethods, safeStaticMethods,
            allTransformClasses);
   }

   @Override
   public MethodAnalysisResults createMethodAnalysisResults()
   {
      return new MethodAnalysisResults();
   }

   @Override
   public void addPath(
         MethodAnalysisResults resultsHolder,
         TypedValue returnValue,
         List<? extends TypedValue> conditions,
         PathAnalysisMethodChecker methodChecker)
   {
      resultsHolder.addPath(returnValue, conditions, methodChecker);
   }

}
