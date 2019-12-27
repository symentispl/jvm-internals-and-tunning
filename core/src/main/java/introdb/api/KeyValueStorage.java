package introdb.api;

import java.io.IOException;
import java.io.Serializable;

public interface KeyValueStorage {

  Object remove(Serializable key) throws IOException, ClassNotFoundException;

  Object get(Serializable key) throws IOException, ClassNotFoundException;;

  void put(Entry entry) throws IOException;

}
