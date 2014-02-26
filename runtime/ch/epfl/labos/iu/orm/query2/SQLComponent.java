package ch.epfl.labos.iu.orm.query2;

import java.util.List;

public interface SQLComponent
{
   void prepareQuery(JDBCQuerySetup setup) throws QueryGenerationException;
   JDBCFragment generateQuery(JDBCQuerySetup setup) throws QueryGenerationException;
   void storeParamLinks(int lambdaIndex, List<ParameterLocation> params) throws QueryGenerationException;
}
