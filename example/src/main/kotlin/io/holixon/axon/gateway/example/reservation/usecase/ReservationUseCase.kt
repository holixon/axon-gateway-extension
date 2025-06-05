package io.holixon.axon.gateway.example.reservation.usecase

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holixon.axon.gateway.example.reservation.client.ExternalPaymentClient
import io.holixon.axon.gateway.example.reservation.event.ReservationConfirmedEvent
import io.holixon.axon.gateway.example.reservation.write.CreateReservationCommand
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

@Component
class ReservationUseCase(
  private val commandGateway: CommandGateway,
  private val externalPaymentClient: ExternalPaymentClient
) {

  private val pendingConfirmations = ConcurrentHashMap<String, MonoSink<ReservationConfirmedEvent>>()

  fun createReservation(reservationId: String): Mono<String> {

    return Mono.create { sink ->

      pendingConfirmations[reservationId] = sink

      commandGateway.send<CreateReservationCommand>(
        CreateReservationCommand(
          reservationId = reservationId, requiresPrePayment = false
        )
      ).whenComplete { result, throwable ->
        if (throwable != null) {
          sink.error(throwable)
        } else {
          logger.info { "CreateReservationCommand Command dispatched: $result" }
        }
      }

    }.timeout(
      Duration.ofSeconds(30) // avoid memory leaks
    ).flatMap { event ->
      externalPaymentClient.call(event.reservationId)
    }.doFinally {
      pendingConfirmations.remove(reservationId)
    }
  }

  @EventHandler
  fun on(event: ReservationConfirmedEvent) {
    pendingConfirmations.remove(event.reservationId)?.success(event)
  }

}