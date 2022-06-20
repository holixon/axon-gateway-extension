package io.holixon.axon.gateway.example

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile

/**
 * Starting point of the application.
 */
fun main(args: Array<String>) {
  SpringApplication.run(AxonGatewayExampleApplication::class.java, *args)
}

/**
 * Example application.
 */
@EnableRevisionAwareQueryGateway
@SpringBootApplication
class AxonGatewayExampleApplication {

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

  /**
   * Factory function creating correlation data provider for revision information.
   * We don't want to explicitly pump revision meta data from command to event.
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

  @Bean
  @Profile("jackson")
  fun objectMapper(): ObjectMapper = jacksonObjectMapper()
    .registerModule(AxonGatewayJacksonModule())

}
