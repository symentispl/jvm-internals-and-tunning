FROM eclipse-temurin:11
ADD https://github.com/jvm-profiling-tools/async-profiler/releases/download/v2.8.3/async-profiler-2.8.3-linux-x64.tar.gz /opt/async-profiler.tar.gz
COPY hsdis-amd64.so /opt/java/openjdk/lib/server/
RUN apt update && apt install --yes linux-tools-generic linux-tools-5.15.0-52-generic && \
    mkdir /opt/async-profiler && tar xzvf /opt/async-profiler.tar.gz --strip 1 -C /opt/async-profiler 