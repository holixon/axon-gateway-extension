package io.holixon.axon.gateway.example

import io.holixon.axon.gateway.configuration.query.EnableRevisionAwareQueryGateway
import org.axonframework.eventhandling.tokenstore.inmemory.InMemoryTokenStore
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

fun main(args: Array<String>) {
  SpringApplication.run(AxonGatewayExampleApplication::class.java, *args)
}

@EnableRevisionAwareQueryGateway
@SpringBootApplication
class AxonGatewayExampleApplication {

  @Bean
  fun inMemoryTokenStore() = InMemoryTokenStore()

  @Bean
  fun inMemoryEventStoreEngine() = InMemoryEventStorageEngine()
}