package ch.epfl.labos.iu.orm.queryll2.path;

public interface PathAnalysisSupplementalFactory<T>
{
   PathAnalysisMethodChecker<T> createMethodChecker();
   MethodAnalysisResults<T> createMethodAnalysisResults();
}
