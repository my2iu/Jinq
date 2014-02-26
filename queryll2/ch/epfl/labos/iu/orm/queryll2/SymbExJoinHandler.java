package ch.epfl.labos.iu.orm.queryll2;

import java.util.List;

import ch.epfl.labos.iu.orm.query2.EntityManagerBackdoor;
import ch.epfl.labos.iu.orm.query2.SQLFragment;
import ch.epfl.labos.iu.orm.query2.SQLSubstitution;

public interface SymbExJoinHandler<T>
{
   void addWhere(SQLFragment where);
   SQLSubstitution.FromReference addFrom(String tableName);
   EntityManagerBackdoor getEntityManager();
   SQLSubstitution.FromReference findExistingJoin(String fromEntity, String name, List<SQLFragment> joinKey);
   void addCachedJoin(String fromEntity, String name, List<SQLFragment> joinKey, SQLSubstitution.FromReference entityTable);
}
