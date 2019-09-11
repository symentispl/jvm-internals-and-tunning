FROM openjdk:8-jdk-slim
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
      build-essential \
      python3 \
      asciidoctor
