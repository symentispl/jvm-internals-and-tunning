package pl.symentis.jvm.example4;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class Record implements KryoSerializable {

	private int value;

	public Record(int i) {
		this.value = i;
	}

	public Record() {
		super();
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public void read(Kryo kryo, Input input) {
		value = input.readInt();
	}

	@Override
	public void write(Kryo kryo, Output output) {
		output.writeInt(value);
	}

}
