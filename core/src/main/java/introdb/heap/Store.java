package introdb.heap;

import java.io.Serializable;

interface Store {

  Object remove(Serializable key);

  Object get(Serializable key);;

  void put(Entry entry);

}
