package pl.symentis.jvm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Livelock {
	
	static class Spoon {
	
		final static Logger LOGGER = LoggerFactory.getLogger(Spoon.class);
		
		private Diner owner;

		public Spoon(Diner d) {
			owner = d;
		}

		public Diner getOwner() {
			return owner;
		}

		public synchronized void setOwner(Diner d) {
			owner = d;
		}

		public synchronized void use() {
			LOGGER.info("{} has eaten!", owner.name);
		}
	}

	static class Diner {
		
		final static Logger LOGGER = LoggerFactory.getLogger(Diner.class);
		
		private String name;
		private boolean isHungry;

		public Diner(String n) {
			name = n;
			isHungry = true;
		}

		public String getName() {
			return name;
		}

		public boolean isHungry() {
			return isHungry;
		}

		public void eatWith(Spoon spoon, Diner spouse) {
			while (isHungry) {
				// Don't have the spoon, so wait patiently for spouse.
				if (spoon.owner != this) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						continue;
					}
					continue;
				}

				// If spouse is hungry, insist upon passing the spoon.
				if (spouse.isHungry()) {
					LOGGER.info("{}: You eat first my darling {}!", name, spouse.getName());
					spoon.setOwner(spouse);
					continue;
				}

				// Spouse wasn't hungry, so finally eat
				spoon.use();
				isHungry = false;
				LOGGER.info("{}: I am stuffed, my darling {}!", name, spouse.getName());
				spoon.setOwner(spouse);
			}
		}
	}

	public static void main(String[] args) {
		final Diner husband = new Diner("Bob");
		final Diner wife = new Diner("Alice");

		final Spoon s = new Spoon(husband);

		new Thread(new Runnable() {
			public void run() {
				husband.eatWith(s, wife);
			}
		}).start();
		new Thread(new Runnable() {
			public void run() {
				wife.eatWith(s, husband);
			}
		}).start();
	}
}