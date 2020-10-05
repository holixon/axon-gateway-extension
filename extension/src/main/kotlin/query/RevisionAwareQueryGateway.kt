package io.holixon.axon.gateway.query

import org.axonframework.common.Registration
import org.axonframework.messaging.GenericMessage
import org.axonframework.messaging.IllegalPayloadAccessException
import org.axonframework.messaging.Message
import org.axonframework.messaging.MessageDispatchInterceptor
import org.axonframework.messaging.responsetypes.ResponseType
import org.axonframework.queryhandling.*
import org.slf4j.LoggerFactory
import reactor.util.concurrent.Queues
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList

/**
 * The revision  gateway taking care of a revision of the projection requested by the query and will deliver
 * results matching at least this revision.
 * @param queryBus bus to use.
 * @param defaultTimeout default timeout to use if not specified in the query.
 */
@Suppress("unused")
class RevisionAwareQueryGateway(
    private val queryBus: QueryBus,
    private val defaultTimeout: Long
) : DefaultQueryGateway(builder().queryBus(queryBus)) {

  companion object {
    @JvmStatic
    private val logger = LoggerFactory.getLogger(RevisionAwareQueryGateway::class.java)
  }

  private val dispatchInterceptors: MutableList<MessageDispatchInterceptor<in QueryMessage<*, *>?>> = CopyOnWriteArrayList()

  override fun <R : Any, Q : Any> query(queryName: String, query: Q, responseType: ResponseType<R>): CompletableFuture<R> {

    @Suppress("UNCHECKED_CAST")
    val queryMessage: Message<Q> = GenericMessage.asMessage(query) as Message<Q>
    val revisionQueryParameter = RevisionQueryParameters.fromMetaData(metaData = queryMessage.metaData)

    return if (revisionQueryParameter == RevisionQueryParameters.NOT_PRESENT) {
      super.query(queryName, query, responseType)
    } else {
      val result = CompletableFuture<R>()
      logger.debug("REVISION-QUERY-GATEWAY-002: Revision-aware query $queryName detected, revision: $revisionQueryParameter")

      val queryTimeout = revisionQueryParameter.getTimeoutOrDefault(defaultTimeout)
      val subscriptionQueryMessage: SubscriptionQueryMessage<Q, R, R> = GenericSubscriptionQueryMessage(
          queryMessage,
          queryName,
          responseType, // use the same response type as in original query for initial result and updates
          responseType
      )

      queryResultPayloadWithMetadata(
          queryResult = queryBus
              .subscriptionQuery(
                  processInterceptors(subscriptionQueryMessage),
                  SubscriptionQueryBackpressure.defaultBackpressure(),
                  Queues.SMALL_BUFFER_SIZE
              ),
          responseType = responseType
      )
          .map {
            logger.debug("REVISION-QUERY-GATEWAY-003: Response received:\n $it")
            it
          }
          .filter { pair -> pair.second.revision >= revisionQueryParameter.minimalRevision }
          .map { pair ->
            logger.debug("REVISION-QUERY-GATEWAY-004: Responded $queryName having $revisionQueryParameter with revision ${pair.second}")
            pair.first
          }
          .timeout(
              Duration.of(queryTimeout, ChronoUnit.SECONDS)
          )
          .subscribe(
              { projectionResult -> result.complete(projectionResult) },
              { exception -> result.completeExceptionally(exception) }
          )

      result
    }
  }

  private fun <R : Any> queryResultPayloadWithMetadata(
      queryResult: SubscriptionQueryResult<QueryResponseMessage<R>, SubscriptionQueryUpdateMessage<R>>,
      responseType: ResponseType<R>
  ) = if (responseType is QueryResponseMessageResponseType<*>) {
    // taking message metadata into account
    queryResult
        .initialResult()
        .filter { initialResult: QueryResponseMessage<R> -> Objects.nonNull(initialResult.payload) }
        .map { obj: QueryResponseMessage<R> -> obj.payload to RevisionValue.fromMetaData(obj.metaData) }
        .onErrorMap { e: Throwable -> if (e is IllegalPayloadAccessException) e.cause else e }
        .concatWith(queryResult
            .updates()
            .filter { update: SubscriptionQueryUpdateMessage<R> -> Objects.nonNull(update.payload) }
            .map { obj: SubscriptionQueryUpdateMessage<R> -> obj.payload to RevisionValue.fromMetaData(obj.metaData) }
            .onErrorMap { e: Throwable -> if (e is IllegalPayloadAccessException) e.cause else e }
        )
  } else {
    // trying to read from payload
    queryResult
        .initialResult()
        .filter { initialResult: QueryResponseMessage<R> -> Objects.nonNull(initialResult.payload) }
        .map { obj: QueryResponseMessage<R> -> obj.payload }
        .onErrorMap { e: Throwable -> if (e is IllegalPayloadAccessException) e.cause else e }
        .concatWith(queryResult
            .updates()
            .filter { update: SubscriptionQueryUpdateMessage<R> -> Objects.nonNull(update.payload) }
            .map { obj: SubscriptionQueryUpdateMessage<R> -> obj.payload }
            .onErrorMap { e: Throwable -> if (e is IllegalPayloadAccessException) e.cause else e }
        )
        .filter { it is Revisionable }
        .map { it to (it as Revisionable).revisionValue } // construct pairs from payload to revision value
  }
  
  override fun registerDispatchInterceptor(interceptor: MessageDispatchInterceptor<in QueryMessage<*, *>?>): Registration {
    dispatchInterceptors.add(interceptor)
    return Registration { dispatchInterceptors.remove(interceptor) }
  }

  private fun <Q, R, T : QueryMessage<Q, R>> processInterceptors(query: T): T {
    var intercepted: T = query
    for (interceptor in dispatchInterceptors) {
      @Suppress("UNCHECKED_CAST")
      intercepted = interceptor.handle(intercepted) as T
    }
    return intercepted
  }

}
