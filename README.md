# Axon Framework - Gateways Extension

[![stable](https://img.shields.io/badge/lifecycle-STABLE-green.svg)](https://github.com/holisticon#open-source-lifecycle)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.holixon.axon.gateway/axon-gateway-extension/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.holixon.axon.gateway/axon-gateway-extension)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/6a2c7585fd5742fbbf288c96023a9af8)](https://www.codacy.com/gh/holixon/axon-gateway-extension/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=holixon/axon-gateway-extension&amp;utm_campaign=Badge_Grade)

_Note:_ This extension is still in an experimental stage.

## Getting started

### Dependencies for the extension

**Maven**

```xml

<dependency>
  <groupId>io.holixon.axon.gateway</groupId>
  <artifactId>axon-gateway-extension</artifactId>
  <version>0.1.0</version>
</dependency>
```

### Dependencies for the Spring Boot Starter

**Maven**

```xml

<dependency>
  <groupId>io.holixon.axon.gateway</groupId>
  <artifactId>axon-gateway-springboot-starter</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Usage

### Revision-Aware Query Gateway

Imagine, you are sending a command, and you want to query for the result of its effect in the projection.
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

Now you can query for a certain revision by, if the result contains the revision information inside the payload
(the query result implements `Revisionable`).

```kotlin
queryGateway.query(
  GenericCommandMessage
    .asCommandMessage<ApprovalRequestQuery>(ApprovalRequestQuery(requestId.trim()))
    .withMetaData(RevisionQueryParameters(revision).toMetaData()),
  ResponseTypes.instanceOf(ApprovalRequestQueryResult::class.java)
).join()
```

As alternative, you can query for a certain revision by if the query method returns `QueryResponseMessage<T>` and carries
metadata inside the message `metadata` field.

```kotlin
queryGateway.query(
  GenericCommandMessage
    .asCommandMessage<ApprovalRequestQuery>(ApprovalRequestQuery(requestId.trim()))
    .withMetaData(RevisionQueryParameters(revision).toMetaData()),
  QueryResponseMessageType.queryResponseMessageType<ApprovalRequest>()
).join()
```

The query gateway will detect the revision metadata in the query and wait until the specified response
with specified revision has arrived in the projection. In order not to wait forever, you can either
pass the timeout with the query or setup default timeout using the application properties.

In order to maintain revisions in your projection, make sure your event get the revision information from
the command and the projection stores it. Currently, the revision must be transported inside the query result,
by implementing the `Revisionable` interface or by returning `QueryResponseMessage<T>` from your handler method.

If you have any questions how to use the extension, please have a look on example project.

### Dispatch-aware command bus

By default, the Axon Server Connector will register all command handlers it finds by AutoConfiguration both on "Local" and "Remote" segments of the command bus. In order to have
more flexibility on that, for example by hiding some CommandHandlers from registration, you might want to have a possibility to exclude CommandHandler registration on a
remote command bus. By doing so, the locally dispatched commands should still get delivered to command handlers by using the local segment of the command bus.

The Dispatch-aware command bus is designed exactly for the purpose above. In order to use it, you will need the Axon Server Connector to be configured
to use Axon Server (`axon.axonserver.enabled` must not be set to `false`). In addition, you need to enable the following property:

```yaml
axon-gateway:
  command:
    dispatch-aware:
      enabled: true
```

Now you need to specify a `CommandDispatchStrategy`. You can do this manually, by providing a bean factory for this, a component 
implementing this interface or by using the predefined components using properties to configure predicates for command names used for 
remote-only registration. For doing so, specify the following properties in your `application.yml` 

```yaml
axon-gateway:
  command:
    dispatch-aware:
      enabled: true
      strategy:
        command-names:
          - io.holixon.axon.gateway.example.UpdateApprovalRequestCommand
          - io.holixon.axon.gateway.example.OtherCommand
        command-packages:
          - example.matching.all.commands.in.this.package.and.sub.packages
```
Please check the implementations of `CommandDispatchStrategy` for more details.

### Jackson for query serialization

If you are using this extension with `Jackson` serialization, it is required that the query response type is
serializable by Jackson. For this purpose, we provide a small Jackson module which needs to be included and registered in your project.

To do so, please add the following dependency to your classpath:

**Maven**

```xml

<dependency>
  <groupId>io.holixon.axon.gateway</groupId>
  <artifactId>axon-gateway-jackson-module</artifactId>
  <version>0.1.0</version>
</dependency>
```

and register it in your Jackson ObjectMapper:

```kotlin
@Bean
fun objectMapper(): ObjectMapper = jacksonObjectMapper()
    .registerModule(AxonGatewayJacksonModule())
```


## Building the extension

If you want to build the extension locally, you need to check it out from GiHub and run the following command:

```shell script
./mvnw clean install
``` 

### Building example project

The project includes an example module demonstrating usage of the extension. If you want to skip the example
build, please run the following command line:

```shell script
./mvnw clean install -DskipExamples
```

To start example please run the `AxonGatewayExampleApplication`. By supplying the Spring profile `inmem` you can run it without the
Axon Server. If you run it with Axon Server, make use of `example/docker-compose.yml` to start one using Docker.

To play with example, navigate to `http://localhost:8079/swagger-ui/index.html` with your browser and send some REST requests.
