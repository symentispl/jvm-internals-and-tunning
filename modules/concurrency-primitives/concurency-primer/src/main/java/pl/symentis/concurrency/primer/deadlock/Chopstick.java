package pl.symentis.concurrency.primer.deadlock;

public class Chopstick {

	private final Integer id;

	public Chopstick(Integer id) {
		super();
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

}
