package introdb.heap;

import java.io.Serializable;

class Entry {

	private final Serializable key;
	private final Serializable value;

	public Entry(Serializable key, Serializable value) {
		super();
		this.key = key;
		this.value = value;
	}

	public Serializable key() {
		return key;
	}

	public Serializable value() {
		return value;
	}

  @Override
  public String toString() {
    var builder = new StringBuilder();
    builder.append("Entry [key=").append(key).append(", value=").append(value).append("]");
    return builder.toString();
  }

}
