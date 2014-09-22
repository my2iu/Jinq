package org.jinq.orm.stream;


/**
 * Create a JinqStream of database data from inside the context of a query. It
 * is a bit of a backdoor hack, but it's needed to support certain queries.
 * <p>
 * 
 * For some queries, it's necessary to create a subquery or join involving a new
 * stream of entities from the database. This normally requires access to the
 * entity manager or connection to the database. Since Java 8 does not provide
 * any reflection capabilities for lambdas, Jinq uses serialization to find
 * their contents. If a query uses an entity manager to create a stream of
 * entities, then the query will need to be passed in an entity manager. Often
 * this entity manager is not serializable, meaning Jinq cannot understand the
 * lambda and generate queries from it.
 * <p>
 * 
 * The InQueryStreamSource is a way for Jinq to pass in a backdoor way to
 * generate streams of entities into lambdas.
 */
public interface InQueryStreamSource
{
   /**
    * Returns a stream of entities using the same database as used
    * by the query being constructed.
    */
   <U> JinqStream<U> stream(Class<U> entityClass);
}
