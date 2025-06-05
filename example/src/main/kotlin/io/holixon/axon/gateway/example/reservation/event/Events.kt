package io.holixon.axon.gateway.example.reservation.event

data class ReservationCreatedEvent(
  val reservationId: String,
  val requiresPrePayment: Boolean
)

data class ReservationConfirmedEvent(
  val reservationId: String,
)