package ch.epfl.labos.iu.orm.queryll2.path;

/**
 * Deprecated legacy interface for storing the results of a static analysis of 
 * class files (not needed since class files are now analyzed at runtime). 
 * 
 *
 */
public interface StaticMethodAnalysisStorage
{
   public abstract void storeMethodAnalysis(String interfaceName,
         String className, MethodAnalysisResults analysis);
}