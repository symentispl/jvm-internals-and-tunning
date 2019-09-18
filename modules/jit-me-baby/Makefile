all: fibonacci jit

init:
	mkdir -p target/bin

fibonacci: init
	gcc -std=c99 -O3 src/main/c/fibonacci.c -o target/bin/fibonacci

jit:
	javac -d target/classes src/main/java/pl/symentis/jit/*.java
