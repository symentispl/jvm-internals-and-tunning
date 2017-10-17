package pl.symentis.jvm;

import java.io.Serializable;

public class Record implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4133010573809141870L;

	private Integer value;

	public Record(Integer i) {
		this.value = i;
	}

	public Record() {
		super();
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

}
