package ch.epfl.labos.iu.orm;

import java.util.Map;

public interface GrouperRealizer<K,V>
{
   public Map<K,V> createRealizedGrouper();
}
