package org.jinq.jpa;

import java.util.Map;

/**
 * Can be passed to a JinqStream as a hint to log all the JPQL
 * queries being issued by Jinq. 
 */
public interface JPAQueryLogger
{
   void logQuery(String query, Map<Integer, Object> positionParameters, 
         Map<String, Object> namedParameters);
}
