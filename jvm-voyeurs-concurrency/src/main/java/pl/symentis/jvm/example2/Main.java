package pl.symentis.jvm.example2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	static class Spoon {

		final static Logger LOGGER = LoggerFactory.getLogger(Spoon.class);

		private Person owner;

		public Spoon(Person d) {
			owner = d;
		}

		public Person getOwner() {
			return owner;
		}

		public synchronized void setOwner(Person d) {
			owner = d;
		}

		public synchronized void use() {
			LOGGER.info("{} has eaten!", owner.name);
		}
	}

	static class Person {

		final static Logger LOGGER = LoggerFactory.getLogger(Person.class);

		private String name;
		private boolean isHungry;

		public Person(String n) {
			name = n;
			isHungry = true;
		}

		public String getName() {
			return name;
		}

		public boolean isHungry() {
			return isHungry;
		}

		public void eatWith(Spoon spoon, Person spouse) {
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
					LOGGER.debug("{}: You eat first my darling {}!", name, spouse.getName());
					spoon.setOwner(spouse);
					continue;
				}

				// Spouse wasn't hungry, so finally eat
				spoon.use();
				isHungry = false;
				LOGGER.debug("{}: I am stuffed, my darling {}!", name, spouse.getName());
				spoon.setOwner(spouse);
			}
		}
	}

	public static void main(String[] args) {
		final Person husband = new Person("Bob");
		final Person wife = new Person("Alice");

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
