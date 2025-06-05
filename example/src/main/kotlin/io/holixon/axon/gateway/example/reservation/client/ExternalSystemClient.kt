package io.holixon.axon.gateway.example.reservation.client

import org.springframework.stereotype.Component

@Component
class ExternalSystemClient {

  fun call(reservationId: String) {
    Thread.sleep(1_000)
  }
}