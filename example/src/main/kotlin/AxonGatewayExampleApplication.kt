package io.holixon.axon.gateway.example

import io.holixon.axon.gateway.configuration.query.EnableRevisionAwareQueryGateway
import org.axonframework.eventhandling.tokenstore.inmemory.InMemoryTokenStore
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

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
   * Produce in-memory event store.
   */
  @Bean
  fun inMemoryEventStoreEngine() = InMemoryEventStorageEngine()
}