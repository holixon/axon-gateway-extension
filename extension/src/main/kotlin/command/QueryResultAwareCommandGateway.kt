package io.holixon.axon.gateway.command

import org.axonframework.commandhandling.CommandCallback
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.IllegalPayloadAccessException
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.QueryMessage
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.concurrent.CompletableFuture


/**
 * Command gateway waiting for the result of the projection received.
 */
class QueryResultAwareCommandGateway(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
    private val defaultTimeout: Long
) {

  /**
   * Sends and waits for the result.
   * @param [P] type of command payload.
   * @param [Q] type of query
   * @param [R] type of query result
   * @param commandMessage command message.
   * @param queryMessage query message.
   * @return completable future from the projection.
   */
  fun <P : Any, Q : Any, R : Any> storeAndWaitForQueryResult(
      commandMessage: CommandMessage<P>,
      queryMessage: QueryMessage<Q, R>): CompletableFuture<R> {

    val subscription = queryGateway
        .subscriptionQuery(queryMessage, queryMessage.responseType, queryMessage.responseType)

    val result = CompletableFuture<R>()
    subscription
        .initialResult()
        .onErrorMap { e: Throwable -> if (e is IllegalPayloadAccessException) e.cause else e }
        .concatWith(subscription
            .updates()
            .onErrorMap { e: Throwable -> if (e is IllegalPayloadAccessException) e.cause else e }
        )
        .timeout(
            Duration.of(defaultTimeout, ChronoUnit.SECONDS)
        )
        .subscribe(
            { projectionResult -> result.complete(projectionResult) },
            { exception -> result.completeExceptionally(exception) }
        )
    commandGateway
        .send(commandMessage, CommandCallback<CommandMessage<P>, Any> { _, commandResultMessage ->
          if (commandResultMessage.isExceptional) {
            result.completeExceptionally(commandResultMessage.exceptionResult())
          }
        })
    return result
  }
}
