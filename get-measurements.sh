#!/usr/bin/env bash
set -ex

# this script gets all measurements which will be needed during presentation

./mvnw clean verify

mkdir target

async_out_dir=$(mktemp -d -p "$(pwd)/target")
java -jar mapreduce-perf/target/benchmarks.jar \
	"SequentialMapReduceWordCountBenchmark.countWords" \
	-f 1 -wi 1 -i 1 \
	-p mapperOutputClass="pl.symentis.mapreduce.mapper.HashMapOutput" \
	-prof jmh.extras.Async:dir="${async_out_dir}"
cp "${async_out_dir}/flame-graph-cpu.svg" SequentialMapReduceWordCountBenchmark-HashMapOutput.svg

async_out_dir=$(mktemp -d -p "$(pwd)/target")
java -jar mapreduce-perf/target/benchmarks.jar \
	"SequentialMapReduceWordCountBenchmark.countWords" \
	-f 1 -wi 1 -i 1 \
	-p mapperOutputClass="pl.symentis.mapreduce.mapper.GuavaMultiMapOutput" \
	-prof jmh.extras.Async:dir="${async_out_dir}"
cp "${async_out_dir}/flame-graph-cpu.svg" SequentialMapReduceWordCountBenchmark-GuavaMultiMapOutput.svg

async_out_dir=$(mktemp -d -p "$(pwd)/target")
java -jar mapreduce-perf/target/benchmarks.jar \
	"SequentialMapReduceWordCountBenchmark.countWords" \
	-f 1 -wi 1 -i 1 \
	-p mapperOutputClass="pl.symentis.mapreduce.mapper.EclipseCollectionsMultiMapOutput" \
	-prof jmh.extras.Async:dir="${async_out_dir}"
cp "${async_out_dir}/flame-graph-cpu.svg" SequentialMapReduceWordCountBenchmark-EclipseCollectionsMultiMapOutput.svg

java -jar mapreduce-perf/target/benchmarks.jar \
	"SequentialMapReduceWordCountBenchmark.countWords" \
	-f 1 \
	-p mapperOutputClass="pl.symentis.mapreduce.mapper.HashMapOutput","pl.symentis.mapreduce.mapper.GuavaMultiMapOutput","pl.symentis.mapreduce.mapper.EclipseCollectionsMultiMapOutput"
