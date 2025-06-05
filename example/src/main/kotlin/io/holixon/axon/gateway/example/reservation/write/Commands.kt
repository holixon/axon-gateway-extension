package io.holixon.axon.gateway.example.reservation.write

import org.axonframework.modelling.command.TargetAggregateIdentifier

data class CreateReservationCommand(
  @TargetAggregateIdentifier
  val reservationId: String,
  val requiresPrePayment: Boolean
)

data class ConfirmReservationCommand(
  @TargetAggregateIdentifier
  val reservationId: String,
)