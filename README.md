# how unit test

	./mvnw clean test

# how run JMH

	./mvnw clean package -DskipTests
	java -jar perf/target/benchmarks.jar