package ch.epfl.labos.iu.orm.queryll2.path;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.jinq.orm.annotations.EntitySupplier;
import org.jinq.orm.annotations.NoSideEffects;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import ch.epfl.labos.iu.orm.queryll2.path.CodePath.PathReturnValueAndConditions;
import ch.epfl.labos.iu.orm.queryll2.symbolic.MethodSignature;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;

public class TransformationClassAnalyzer
{
   // Mapping of transformation classes to the name of the method
   // containing the actual transformation
   public static Map<String, String> TransformationClassMethods =
      new HashMap<String, String>();
   public static Set<MethodSignature> KnownSafeMethods =
      new HashSet<MethodSignature>();
   public static Set<MethodSignature> KnownSafeStaticMethods =
      new HashSet<MethodSignature>();
   public static Set<Class<?>> SafeMethodAnnotations =
      new HashSet<Class<?>>();
   
   public static String WHERE_INTERFACE = "ch/epfl/labos/iu/orm/DBSet$Where";
   public static String WHERE_METHOD = "where";
   public static String SELECT_INTERFACE = "ch/epfl/labos/iu/orm/DBSet$Select";
   public static String SELECT_METHOD = "select";
   public static String AGGREGATESELECT_INTERFACE = "ch/epfl/labos/iu/orm/DBSet$AggregateSelect";
   public static String AGGREGATESELECT_METHOD = "aggregateSelect";
   public static String AGGREGATEINTEGER_INTERFACE = "ch/epfl/labos/iu/orm/DBSet$AggregateInteger";
   public static String AGGREGATEINTEGER_METHOD = "aggregate";
   public static String AGGREGATEDOUBLE_INTERFACE = "ch/epfl/labos/iu/orm/DBSet$AggregateDouble";
   public static String AGGREGATEDOUBLE_METHOD = "aggregate";
   public static String JOIN_INTERFACE = "ch/epfl/labos/iu/orm/DBSet$Join";
   public static String JOIN_METHOD = "join";
   public static String GROUP_INTERFACE = "ch/epfl/labos/iu/orm/DBSet$AggregateGroup";
   public static String GROUP_METHOD = "aggregateSelect";
   public static String DATESORTER_SUPERCLASS = "ch/epfl/labos/iu/orm/DateSorter";
   public static String DATESORTER_METHOD = "value";
   public static String INTSORTER_SUPERCLASS = "ch/epfl/labos/iu/orm/IntSorter";
   public static String INTSORTER_METHOD = "value";
   public static String DOUBLESORTER_SUPERCLASS = "ch/epfl/labos/iu/orm/DoubleSorter";
   public static String DOUBLESORTER_METHOD = "value";
   public static String STRINGSORTER_SUPERCLASS = "ch/epfl/labos/iu/orm/StringSorter";
   public static String STRINGSORTER_METHOD = "value";
   public static String DBSET_CLASS = "Lch/epfl/labos/iu/orm/DBSet;";
   public static String JINQSTREAM_CLASS = "Lorg/jinq/orm/stream/JinqStream;";
   public static MethodSignature stringEquals = new MethodSignature("java/lang/String", "equals", "(Ljava/lang/Object;)Z");
   public static MethodSignature stringLike = new MethodSignature("ch/epfl/labos/iu/orm/Util", "SQLStringLike", "(Ljava/lang/String;Ljava/lang/String;)Z");
   public static MethodSignature newPair = new MethodSignature("ch/epfl/labos/iu/orm/Pair", "<init>", "(Ljava/lang/Object;Ljava/lang/Object;)V");
   public static MethodSignature pairGetOne = new MethodSignature("ch/epfl/labos/iu/orm/Pair", "getOne", "()Ljava/lang/Object;");
   public static MethodSignature pairGetTwo = new MethodSignature("ch/epfl/labos/iu/orm/Pair", "getTwo", "()Ljava/lang/Object;");
   public static MethodSignature newTuple3 = new MethodSignature("ch/epfl/labos/iu/orm/Tuple3", "<init>", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V");
   public static MethodSignature tuple3GetOne = new MethodSignature("ch/epfl/labos/iu/orm/Tuple3", "getOne", "()Ljava/lang/Object;");
   public static MethodSignature tuple3GetTwo = new MethodSignature("ch/epfl/labos/iu/orm/Tuple3", "getTwo", "()Ljava/lang/Object;");
   public static MethodSignature tuple3GetThree = new MethodSignature("ch/epfl/labos/iu/orm/Tuple3", "getThree", "()Ljava/lang/Object;");
   public static MethodSignature newTuple4 = new MethodSignature("ch/epfl/labos/iu/orm/Tuple4", "<init>", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V");
   public static MethodSignature tuple4GetOne = new MethodSignature("ch/epfl/labos/iu/orm/Tuple4", "getOne", "()Ljava/lang/Object;");
   public static MethodSignature tuple4GetTwo = new MethodSignature("ch/epfl/labos/iu/orm/Tuple4", "getTwo", "()Ljava/lang/Object;");
   public static MethodSignature tuple4GetThree = new MethodSignature("ch/epfl/labos/iu/orm/Tuple4", "getThree", "()Ljava/lang/Object;");
   public static MethodSignature tuple4GetFour = new MethodSignature("ch/epfl/labos/iu/orm/Tuple4", "getFour", "()Ljava/lang/Object;");
   public static MethodSignature newTuple5 = new MethodSignature("ch/epfl/labos/iu/orm/Tuple5", "<init>", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V");
   public static MethodSignature tuple5GetOne = new MethodSignature("ch/epfl/labos/iu/orm/Tuple5", "getOne", "()Ljava/lang/Object;");
   public static MethodSignature tuple5GetTwo = new MethodSignature("ch/epfl/labos/iu/orm/Tuple5", "getTwo", "()Ljava/lang/Object;");
   public static MethodSignature tuple5GetThree = new MethodSignature("ch/epfl/labos/iu/orm/Tuple5", "getThree", "()Ljava/lang/Object;");
   public static MethodSignature tuple5GetFour = new MethodSignature("ch/epfl/labos/iu/orm/Tuple5", "getFour", "()Ljava/lang/Object;");
   public static MethodSignature tuple5GetFive = new MethodSignature("ch/epfl/labos/iu/orm/Tuple5", "getFive", "()Ljava/lang/Object;");
   public static MethodSignature newTuple8 = new MethodSignature("ch/epfl/labos/iu/orm/Tuple8", "<init>", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V");
   public static MethodSignature tuple8GetOne = new MethodSignature("ch/epfl/labos/iu/orm/Tuple8", "getOne", "()Ljava/lang/Object;");
   public static MethodSignature tuple8GetTwo = new MethodSignature("ch/epfl/labos/iu/orm/Tuple8", "getTwo", "()Ljava/lang/Object;");
   public static MethodSignature tuple8GetThree = new MethodSignature("ch/epfl/labos/iu/orm/Tuple8", "getThree", "()Ljava/lang/Object;");
   public static MethodSignature tuple8GetFour = new MethodSignature("ch/epfl/labos/iu/orm/Tuple8", "getFour", "()Ljava/lang/Object;");
   public static MethodSignature tuple8GetFive = new MethodSignature("ch/epfl/labos/iu/orm/Tuple8", "getFive", "()Ljava/lang/Object;");
   public static MethodSignature tuple8GetSix = new MethodSignature("ch/epfl/labos/iu/orm/Tuple8", "getSix", "()Ljava/lang/Object;");
   public static MethodSignature tuple8GetSeven = new MethodSignature("ch/epfl/labos/iu/orm/Tuple8", "getSeven", "()Ljava/lang/Object;");
   public static MethodSignature tuple8GetEight = new MethodSignature("ch/epfl/labos/iu/orm/Tuple8", "getEight", "()Ljava/lang/Object;");
   public static MethodSignature streamSumInt = new MethodSignature("org/jinq/orm/stream/JinqStream", "sumInt", "(Lorg/jinq/orm/stream/JinqStream$AggregateInteger;)I");
   public static MethodSignature streamSumDouble = new MethodSignature("org/jinq/orm/stream/JinqStream", "sumDouble", "(Lorg/jinq/orm/stream/JinqStream$AggregateDouble;)D");
   public static MethodSignature streamMaxInt = new MethodSignature("org/jinq/orm/stream/JinqStream", "maxInt", "(Lorg/jinq/orm/stream/JinqStream$AggregateInteger;)I");
   public static MethodSignature streamMaxDouble = new MethodSignature("org/jinq/orm/stream/JinqStream", "maxDouble", "(Lorg/jinq/orm/stream/JinqStream$AggregateDouble;)D");
   public static MethodSignature dbsetSumInt = new MethodSignature("ch/epfl/labos/iu/orm/DBSet", "sumInt", "(Lch/epfl/labos/iu/orm/DBSet$AggregateInteger;)I");
   public static MethodSignature dbsetSumDouble = new MethodSignature("ch/epfl/labos/iu/orm/DBSet", "sumDouble", "(Lch/epfl/labos/iu/orm/DBSet$AggregateDouble;)D");
   public static MethodSignature dbsetMaxInt = new MethodSignature("ch/epfl/labos/iu/orm/DBSet", "maxInt", "(Lch/epfl/labos/iu/orm/DBSet$AggregateInteger;)I");
   public static MethodSignature dbsetMaxDouble = new MethodSignature("ch/epfl/labos/iu/orm/DBSet", "maxDouble", "(Lch/epfl/labos/iu/orm/DBSet$AggregateDouble;)D");
   public static MethodSignature dbsetWhere = new MethodSignature("ch/epfl/labos/iu/orm/DBSet", "where", "(Lch/epfl/labos/iu/orm/DBSet$Where;)Lch/epfl/labos/iu/orm/DBSet;");
   public static MethodSignature dbsetToStream = new MethodSignature("ch/epfl/labos/iu/orm/DBSet", "jinqStream", "()Lorg/jinq/orm/stream/JinqStream;");
   public static MethodSignature integerValueOf = new MethodSignature("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
   public static MethodSignature doubleValueOf = new MethodSignature("java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
   public static MethodSignature integerIntValue = new MethodSignature("java/lang/Integer", "intValue", "()I");
   public static MethodSignature doubleDoubleValue = new MethodSignature("java/lang/Double", "doubleValue", "()D");
   static {
      TransformationClassMethods.put(GROUP_INTERFACE, GROUP_METHOD);
      TransformationClassMethods.put(JOIN_INTERFACE, JOIN_METHOD);
      TransformationClassMethods.put(AGGREGATEDOUBLE_INTERFACE, AGGREGATEDOUBLE_METHOD);
      TransformationClassMethods.put(AGGREGATEINTEGER_INTERFACE, AGGREGATEINTEGER_METHOD);
      TransformationClassMethods.put(AGGREGATESELECT_INTERFACE, AGGREGATESELECT_METHOD);
      TransformationClassMethods.put(SELECT_INTERFACE, SELECT_METHOD);
      TransformationClassMethods.put(WHERE_INTERFACE, WHERE_METHOD);
      TransformationClassMethods.put(DATESORTER_SUPERCLASS, DATESORTER_METHOD);
      TransformationClassMethods.put(INTSORTER_SUPERCLASS, INTSORTER_METHOD);
      TransformationClassMethods.put(DOUBLESORTER_SUPERCLASS, DOUBLESORTER_METHOD);
      TransformationClassMethods.put(STRINGSORTER_SUPERCLASS, STRINGSORTER_METHOD);
      KnownSafeMethods.add(stringEquals);
      KnownSafeStaticMethods.add(stringLike);
      KnownSafeMethods.add(newPair);
      KnownSafeMethods.add(pairGetOne);
      KnownSafeMethods.add(pairGetTwo);
      KnownSafeMethods.add(newTuple3);
      KnownSafeMethods.add(tuple3GetOne);
      KnownSafeMethods.add(tuple3GetTwo);
      KnownSafeMethods.add(tuple3GetThree);
      KnownSafeMethods.add(newTuple4);
      KnownSafeMethods.add(tuple4GetOne);
      KnownSafeMethods.add(tuple4GetTwo);
      KnownSafeMethods.add(tuple4GetThree);
      KnownSafeMethods.add(tuple4GetFour);
      KnownSafeMethods.add(newTuple5);
      KnownSafeMethods.add(tuple5GetOne);
      KnownSafeMethods.add(tuple5GetTwo);
      KnownSafeMethods.add(tuple5GetThree);
      KnownSafeMethods.add(tuple5GetFour);
      KnownSafeMethods.add(tuple5GetFive);
      KnownSafeMethods.add(newTuple8);
      KnownSafeMethods.add(tuple8GetOne);
      KnownSafeMethods.add(tuple8GetTwo);
      KnownSafeMethods.add(tuple8GetThree);
      KnownSafeMethods.add(tuple8GetFour);
      KnownSafeMethods.add(tuple8GetFive);
      KnownSafeMethods.add(tuple8GetSix);
      KnownSafeMethods.add(tuple8GetSeven);
      KnownSafeMethods.add(tuple8GetEight);
      SafeMethodAnnotations.add(NoSideEffects.class);
      SafeMethodAnnotations.add(EntitySupplier.class);
   }
   
   ClassNode cl = new ClassNode();
   public TransformationClassAnalyzer(File f) throws IOException
   {
      FileInputStream fis = new FileInputStream(f);
      ClassReader reader = new ClassReader(fis);
      reader.accept(cl,0);
      fis.close();
   }

   public TransformationClassAnalyzer(String className) throws IOException
   {
      ClassReader reader = new ClassReader(className);
      reader.accept(cl,0);
   }
   
   public <T, U> void analyze(StaticMethodAnalysisStorage analysisResults, PathAnalysisSupplementalFactory<T, U> pathAnalysisFactory)
   {
      // TODO: Analyze the constructor
      
      // Find the method containing the transformation code
      List<String> interfacesAndParent = new ArrayList<String>();
      interfacesAndParent.addAll((List<String>)cl.interfaces);
      interfacesAndParent.add(cl.superName);
      for (String i: interfacesAndParent)
      {
         String expectedMethodName = TransformationClassMethods.get(i);
         if (expectedMethodName == null) continue;
         MethodNode nonSpecificMethod = null;
         MethodNode specificMethod = null;
         int specificity = Integer.MAX_VALUE;
         for (MethodNode m: (List<MethodNode>)cl.methods)
         {
            if (!m.name.equals(expectedMethodName))
               continue;
            // TODO: Check the method parameters properly here instead
            // of this quick hack
            if (m.desc.split("Ljava/lang/Object").length < specificity)
            {
               specificMethod = m;
               specificity = m.desc.split("Ljava/lang/Object").length;
            }
            else
               nonSpecificMethod = m;
         }
         
         // TODO: Right now we assume that there is always a specific
         // (reified) method and that we can ignore the generic version
         // of the method. Plus we assume that the generic version of the
         // method always passes control directly to the specific version
         try {
//            System.out.println(specificMethod.name + " " + specificMethod.signature + " " + specificMethod.desc);
            U analysis = analyzeMethod(specificMethod, pathAnalysisFactory);
            if (analysis != null)
            {
               analysisResults.storeMethodAnalysis(i, cl.name, analysis);
//               System.out.println("Ok");
            }
         } catch(AnalyzerException e)
         {
            e.printStackTrace();
         }
      }
   }

   public <T, U> U analyzeLambdaMethod(String methodName, String methodSignature, PathAnalysisSupplementalFactory<T, U> pathAnalysisFactory) throws AnalyzerException
   {
      MethodNode specificMethod = null;
      for (MethodNode m: (List<MethodNode>)cl.methods)
      {
         if (!m.name.equals(methodName) || !m.desc.equals(methodSignature))
            continue;
         specificMethod = m;
      }
      if (specificMethod != null)
         return analyzeMethod(specificMethod, pathAnalysisFactory);
      return null;
   }
   
   <T, U> U analyzeMethod(MethodNode m, PathAnalysisSupplementalFactory<T, U> pathAnalysisFactory) throws AnalyzerException
   {
      // TODO: Various checks (e.g. no try/catch blocks, exceptions, etc.)
      if (m.tryCatchBlocks.size() > 0) return null;
      
      // Build a control flow graph
      //
      CFG cfg = new CFG(cl.name, m);
      
      // Given the index of an instruction, returns a list of indices of 
      // successor instructions.
      
      // Check that the control flow graph has no loops.
      int[]visitStatus = new int[m.instructions.size()];  
      if (hasLoops(cfg, visitStatus, 0)) return null;
      
      // TODO: Check for instructions causing side-effects
      
      // Break the code into paths
      List<CodePath> paths = CodePath.breakIntoPaths(cfg, m, cl.name);
      
      // Symbolically execute each path to figure out what each path does
      U analysis = pathAnalysisFactory.createMethodAnalysisResults();
      for (CodePath path: paths)
      {
         PathAnalysisMethodChecker methodChecker = pathAnalysisFactory.createMethodChecker();

         PathReturnValueAndConditions pathResults = 
            path.calculateReturnValueAndConditions(cl, m, methodChecker);
         pathAnalysisFactory.addPath(analysis, pathResults.returnValue, pathResults.conditions, methodChecker);
      }
      return analysis;
   }
   
   boolean hasLoops(CFG cfg, int[]visitStatus, int index)
   {
      final int VISITING = 1;
      final int VISITED = 2;
      
      // reached an instruction with no successor (probably a return instruction)
      if (cfg.succsOf(index) == null) return false;
      
      // Check if we've visited the node and finished exploring all sub-branches
      if (visitStatus[index] == VISITED) return false;

      // Check if we've looped back to visit a node we haven't finished exploring
      if (visitStatus[index] == VISITING) return true;
      
      // Mark node as being explored
      visitStatus[index] = VISITING;
      
      // Visit successors of this node and see if we loop back
      for (int succ: cfg.succsOf(index))
         if (hasLoops(cfg, visitStatus, succ))
            return true;
      
      // Mark this node as having been explored successfully 
      visitStatus[index] = VISITED;
      return false;
   }
}
