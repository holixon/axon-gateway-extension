# Axon Framework - Gateways Extension

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.holixon/axon-gateway-extension/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.axonframework.extensions.kotlin/axon-kotlin)

_Note:_ This extension is still in an experimental stage.


## Getting started

### Dependencies for the extension

**Maven**

```
<dependency>
    <groupId>io.holixon</groupId>
    <artifactId>axon-gateway-extension</artifactId>
    <version>0.0.1</version>
</dependency>
```

**Gradle**

```
implementation("io.holixon:axon-gateway-extension:0.0.1")
```

## Receiving help

## Feature requests and issue reporting

## Building the extension

If you want to build the extension locally, you need to check it out from GiHub and run the following command:

    ./mvnw clean install

### Producing JavaDocs and Sources archive

Please execute the following command line if you are interested in producing KDoc and Source archives:

    ./mvnw clean install -Pdocs-and-sources

### Collecting code coverage data

If you are interested in test code coverage, please run the following command:

    ./mvnw clean install -Pcoverage

### Building example project

The project includes an example module demonstrating usage of the extension. If you want to skip the example
build, please run the following command line:

    ./mvnw clean install -DskipExamples

---
