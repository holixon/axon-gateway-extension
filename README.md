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
    <version>0.0.2</version>
</dependency>
```

**Gradle**
```
implementation("io.holixon:axon-gateway-extension:0.0.2")
```

### Dependencies for the Spring Boot Starter

**Maven**
```
<dependency>
    <groupId>io.holixon</groupId>
    <artifactId>axon-gateway-springboot-starter</artifactId>
    <version>0.0.2</version>
</dependency>
```

**Gradle**
```
implementation("io.holixon:axon-gateway-springboot-starter:0.0.2")
```

## Usage

### Revision-Aware Gateway

Imagine, you are sending a command and you want to query for the result of its effect in the projection.
A Revision-Aware Gateway is capable of retrieving for a certain (minimal) revision of the projection. In 
order to do so, you need to pass the revision along with your command. To do so, add the metadata using the 
helper method:

```kotlin
commandGateway.send<Void>(
    GenericCommandMessage.asCommandMessage<UpdateApprovalRequestCommand>(
        CreateApprovalRequestCommand(
            requestId = requestId,
            subject = value.subject,
            currency = value.currency,
            amount = value.amount
        )
    ).withMetaData(RevisionValue(revision).toMetaData())
)
```

Now you can query for a certain revision by:

```kotlin
queryGateway.query(
        GenericCommandMessage
            .asCommandMessage<ApprovalRequestQuery>(ApprovalRequestQuery(requestId.trim()))
            .withMetaData(RevisionQueryParameters(revision).toMetaData()),
        ResponseTypes.instanceOf(ApprovalRequestQueryResult::class.java)
    ).join()
```
The gateway will detect the revision metadata in the query and wait until the specified response
with specified revision has arrived in the projection. In order not to wait forever, you can either
pass the timeout with the query or setup default timeout using the application properties.

In order to maintain revisions in your projection, make sure your event get the revision information from
the command and the projection stores it. Currently, the revision must be transported inside the query result,
by implementing the `Revisionable` interface.

If you have any questions how to use the extension, please have a look on example project.   

## Building the extension

If you want to build the extension locally, you need to check it out from GiHub and run the following command:

```
./mvnw clean install
``` 

### Building example project

The project includes an example module demonstrating usage of the extension. If you want to skip the example
build, please run the following command line:

```
./mvnw clean install -DskipExamples
```

