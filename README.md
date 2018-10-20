# how to unit test

	./mvnw clean test

# how to run JMH

	./mvnw clean package -DskipTests
	java -jar perf/target/benchmarks.jar