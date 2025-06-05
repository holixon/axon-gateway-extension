package io.holixon.axon.gateway.example.reservation.rest

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holixon.axon.gateway.example.reservation.usecase.ReservationUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/reservation")
class ReservationController(
  private val reservationUseCase: ReservationUseCase
) {

  @PostMapping("/create")
  fun createReservation(): ResponseEntity<Void> {

    val reservationId = UUID.randomUUID().toString()

    logger.info { "REST: Creating reservation with id $reservationId" }

    val paymentLink: String = requireNotNull(reservationUseCase.createReservation(reservationId).block())

    logger.info { "REST: Created, link: $paymentLink." }

    return ResponseEntity.created(URI.create(paymentLink)).build()
  }
}