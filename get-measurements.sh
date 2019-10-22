#!/usr/bin/env bash
set -ex

# this script gets all measurements which will be needed during presentation

if [[ -z "$FLAME_GRAPH_DIR" ]]
then
	echo "missing FLAME_GRAPH_DIR environment variable"
	exit 1
fi


boxes -d stone <<< "Building benchmarks"

./mvnw -l build.log clean verify

forks=1
iterations=1
warmups=1

results=target/results

mkdir -p "${results}"

async_profile() {
	local benchmarks_basepackage="$1"
	shift
	local parameter_baskepackage="$1"
	shift
	local parameter_name="$1"
	shift	
	local parameter_classnames=("$@")

	for classname in "${parameter_classnames[@]}"
	do
		profiler_output_dir=$(mktemp -d -p "$(pwd)/target")
		java -jar mapreduce-perf/target/benchmarks.jar \
			"${benchmarks_basepackage}.SequentialMapReduceWordCountBenchmark.countWords" \
			-f $forks -wi $warmups -i $iterations \
			-p "${parameter_name}"="${parameter_baskepackage}"."${classname}" \
			-prof jmh.extras.Async:dir="${profiler_output_dir}" 
		cp "${profiler_output_dir}/flame-graph-cpu.svg" "${results}/SequentialMapReduceWordCountBenchmark-${classname}.svg"
	done
}

jfr_profile() {
	local benchmark="$1"
	shift
	local parameter_name="$1"
	shift	
	local parameter_values=("$@")
	for value in "${parameter_values[@]}"
	do
		profiler_output_dir=$(mktemp -d -p "$(pwd)/target")
		java -jar mapreduce-perf/target/benchmarks.jar \
			"${benchmark}" \
			-f $forks -wi $warmups -i $iterations \
			-p "${parameter_name}"="${value}" \
			-prof pl.symentis.jmh.profilers.JFR:dir="${profiler_output_dir}"
		cp "${profiler_output_dir}/profile.jfr" "${results}/${benchmark}-${parameter_name}-$value.jfr"
	done
}

measurment() {
	local benchmarks_basepackage="$1"
	shift
	local parameter_baskepackage="$1"
	shift
	local parameter_name="$1"
	shift	
	local parameter_classnames=("$@")
	declare -a all_parameter_classnames

	for classname in "${parameter_classnames[@]}"
	do
		all_parameter_classnames+=("${parameter_baskepackage}.${classname}")
	done

	java -jar mapreduce-perf/target/benchmarks.jar \
		"${benchmarks_basepackage}.SequentialMapReduceWordCountBenchmark.countWords" \
		-f $forks -wi $warmups -i $iterations \
		-p "${parameter_name}"=""$(IFS=, ; echo "${all_parameter_classnames[*]}")"" \
		-rff "${results}/SequentialMapReduceWordCountBenchmark-${parameter_name}.csv"		
}

benchmarks_basepackage="pl.symentis.wordcount"

boxes -d stone <<< "Sequential map reduce - comparing stopwords implementations"

stopwords_basepackage="pl.symentis.wordcount.stopwords"
stopwords=("NonThreadLocalStopwords" "ThreadLocalStopwords" "ICUThreadLocalStopwords")
async_profile "$benchmarks_basepackage" "$stopwords_basepackage" "stopwordsClass" "${stopwords[@]}"
measurment "$benchmarks_basepackage" "$stopwords_basepackage" "stopwordsClass" "${stopwords[@]}"

boxes -d stone <<< "Sequential map reduce - comparing mapper output implementations"

mappers_outputs_basepackage="pl.symentis.mapreduce.mapper"
mappers_outputs=("HashMapOutput" "GuavaMultiMapOutput" "EclipseCollectionsMultiMapOutput")
async_profile "$benchmarks_basepackage" "$mappers_outputs_basepackage" "mapperOutputClass" "${mappers_outputs[@]}"
measurment "$benchmarks_basepackage" "$mappers_outputs_basepackage" "mapperOutputClass" "${mappers_outputs[@]}"

boxes -d stone <<< "Parallel map reduce"

jfr_profile pl.symentis.wordcount.ParallelMapReduceWordCountBenchmark threadPoolSize 4 8 16 32

boxes -d stone <<< "Batching parallel map reduce"

jfr_profile pl.symentis.wordcount.BatchingParallelMapReduceWordCountBenchmark threadPoolSize 4 8 16 32

boxes -d stone <<< "ForkJoin map reduce"

jfr_profile pl.symentis.wordcount.ForkJoinMapReduceWordCountBenchmark threadPoolSize 4 8 16 32

boxes -d stone <<< "Comparing all implementations"

java -jar mapreduce-perf/target/benchmarks.jar \
	-f $forks -wi $warmups -i $iterations \
	-rff "${results}/all-benchmarks.csv"
