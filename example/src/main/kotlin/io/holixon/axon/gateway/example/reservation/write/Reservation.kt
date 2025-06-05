package io.holixon.axon.gateway.example.reservation.write

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holixon.axon.gateway.example.reservation.event.ReservationConfirmedEvent
import io.holixon.axon.gateway.example.reservation.event.ReservationCreatedEvent
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate

private val logger = KotlinLogging.logger {}

@Aggregate
class Reservation() {
  @AggregateIdentifier
  lateinit var reservationId: String

  @CommandHandler
  constructor(command: CreateReservationCommand): this() {
    logger.info { "Creating reservation..." }
    AggregateLifecycle.apply(
      ReservationCreatedEvent(
        reservationId = command.reservationId,
        requiresPrePayment = command.requiresPrePayment
      )
    )
    logger.info { "Reservation created: ${command.reservationId}." }
  }

  @CommandHandler
  fun handle(command: ConfirmReservationCommand) {
    logger.info { "Confirming reservation created..." }
    AggregateLifecycle.apply(
      ReservationConfirmedEvent(
        reservationId = command.reservationId
      )
    )
    logger.info { "Reservation confirmed: $reservationId." }
  }


  @EventSourcingHandler
  fun onReservationCreated(event: ReservationCreatedEvent) {
    this.reservationId = event.reservationId
  }
}