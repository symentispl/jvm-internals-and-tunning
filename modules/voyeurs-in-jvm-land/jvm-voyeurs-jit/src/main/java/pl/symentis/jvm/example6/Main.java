package pl.symentis.jvm.example6;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
	
	static final int CHUNK_SIZE = 1000;

	private static Object uncommonTrap(Object trap) {
		if (trap != null) {
			LOGGER.debug("I am being trapped!");
		}
		return trap;
	}

	public static void main(String[] argv) {
		while (true) {
			Object trap = null;
			for (int i = 0; i < 250; ++i) {
				for (int j = 0; j < CHUNK_SIZE; ++j) {
					trap = uncommonTrap(trap);
				}
				if (i == 200) {
					trap = new Object();
				}
			}
		}
	}

}
