package io.holixon.axon.gateway.example.reservation.client

import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*

@Component
class ExternalPaymentClient {

  fun call(reservationId: String): Mono<String> {

    Thread.sleep(5_000)

    return Mono.just("https://url/${UUID.randomUUID()}")
  }
}