package io.holixon.axon.gateway.example

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.security.AnyTypePermission
import io.holixon.axon.gateway.configuration.query.EnableRevisionAwareQueryGateway
import io.holixon.axon.gateway.jackson.module.AxonGatewayJacksonModule
import io.holixon.axon.gateway.query.RevisionValue
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.eventhandling.tokenstore.inmemory.InMemoryTokenStore
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine
import org.axonframework.messaging.correlation.CorrelationDataProvider
import org.axonframework.messaging.correlation.MessageOriginProvider
import org.axonframework.messaging.correlation.MultiCorrelationDataProvider
import org.axonframework.messaging.correlation.SimpleCorrelationDataProvider
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * Starting point of the application.
 */
fun main(args: Array<String>) {
  runApplication<AxonGatewayExampleApplication>(*args)
}

/**
 * Example application.
 */
@EnableRevisionAwareQueryGateway
@SpringBootApplication
class AxonGatewayExampleApplication {

  /**
   * Factory function creating correlation data provider for revision information.
   * We don't want to explicitly pump revision metadata from command to event.
   */
  @Bean
  fun revisionAwareCorrelationDataProvider(): CorrelationDataProvider {
    return MultiCorrelationDataProvider<CommandMessage<Any>>(
      listOf(
        MessageOriginProvider(),
        SimpleCorrelationDataProvider(RevisionValue.REVISION_KEY)
      )
    )
  }

  /**
   * Provides an object mapper using the Jackson module.
   */
  @Bean
  @Profile("jackson")
  fun objectMapper(): ObjectMapper = jacksonObjectMapper()
    .registerModule(AxonGatewayJacksonModule())


  @Bean
  fun unsafeXstream() = XStream().apply {
    addPermission(AnyTypePermission.ANY)
  }

  /**
   * In-Memory configuration working without Axon Server.
   */
  @Configuration
  @Profile("inmem")
  class InMemoryConfiguration {
    /**
     * Produce in-memory token store for the projection.
     */
    @Bean
    fun inMemoryTokenStore() = InMemoryTokenStore()

    /**
     * Produce in-memory event store, since we play locally.
     */
    @Bean
    fun inMemoryEventStoreEngine() = InMemoryEventStorageEngine()
  }
}
