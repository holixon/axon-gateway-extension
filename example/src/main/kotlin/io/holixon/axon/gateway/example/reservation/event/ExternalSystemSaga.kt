package io.holixon.axon.gateway.example.reservation.event

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.oshai.kotlinlogging.KotlinLogging
import io.holixon.axon.gateway.example.reservation.client.ExternalSystemClient
import io.holixon.axon.gateway.example.reservation.write.ConfirmReservationCommand
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.modelling.saga.EndSaga
import org.axonframework.modelling.saga.SagaEventHandler
import org.axonframework.modelling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.springframework.beans.factory.annotation.Autowired

private val logger = KotlinLogging.logger {}

@Saga
class ExternalSystemSaga {

  lateinit var reservationId: String

  @Autowired
  @Transient
  @JsonIgnore
  lateinit var commandGateway: CommandGateway

  @Autowired
  @Transient
  @JsonIgnore
  lateinit var externalSystemClient: ExternalSystemClient


  @StartSaga
  @SagaEventHandler(associationProperty = "reservationId")
  fun on(event: ReservationCreatedEvent) {
    logger.info { "Started saga for: ${event.reservationId}" }
    reservationId = event.reservationId
    transferReservation()
  }

  @EndSaga
  @SagaEventHandler(associationProperty = "reservationId")
  fun on(event: ReservationConfirmedEvent) {
    logger.info { "Finished saga for: ${event.reservationId}" }
  }

  fun transferReservation() {
    logger.info { "Transferring to external system." }

    externalSystemClient.call(reservationId = reservationId)

    logger.info { "Transferred to external system." }
    commandGateway.send<ConfirmReservationCommand>(
      ConfirmReservationCommand(this.reservationId)
    ).join()
  }
}