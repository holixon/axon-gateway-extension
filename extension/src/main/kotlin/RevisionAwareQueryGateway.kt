package io.holixon.axon.gateway.query

import mu.KLogging
import org.axonframework.common.Registration
import org.axonframework.messaging.GenericMessage
import org.axonframework.messaging.IllegalPayloadAccessException
import org.axonframework.messaging.Message
import org.axonframework.messaging.MessageDispatchInterceptor
import org.axonframework.messaging.responsetypes.ResponseType
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.*
import org.reactivestreams.Publisher
import reactor.util.concurrent.Queues
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList

/**
 * The revision  gateway taking care of a revision of the projection requested by the query and will deliver
 * results matching at leas this revision.
 * @param queryBus bus to use.
 * @param defaultTimeout default timeout to use if not specified in the query.
 */
@Suppress("unused")
class RevisionAwareQueryGateway(
        private val queryBus: QueryBus,
        private val defaultTimeout: Long
) : DefaultQueryGateway(builder().queryBus(queryBus)) {

    private val dispatchInterceptors: MutableList<MessageDispatchInterceptor<in QueryMessage<*, *>?>> = CopyOnWriteArrayList()

    companion object : KLogging()

    override fun <R : Any, Q : Any> query(queryName: String, query: Q, responseType: ResponseType<R>): CompletableFuture<R> {

        @Suppress("UNCHECKED_CAST")
        val queryMessage: Message<Q> = GenericMessage.asMessage(query) as Message<Q>
        val revisionQueryParameter = RevisionQueryParameters.fromMetaData(metaData = queryMessage.metaData)

        return if (revisionQueryParameter == RevisionQueryParameters.NOT_PRESENT) {
            super.query(queryName, query, responseType)
        } else {
            val result = CompletableFuture<R>()
            logger.debug { "REVISION-QUERY-GATEWAY-002: Revision-aware query $queryName detected, revision: $revisionQueryParameter" }

            val queryTimeout = revisionQueryParameter.getTimeoutOrDefault(defaultTimeout)

            @Suppress("UNCHECKED_CAST")
            val subscriptionQueryMessage: SubscriptionQueryMessage<Q, R, R> = GenericSubscriptionQueryMessage(
                    queryMessage,
                    queryName,
                    ResponseTypes.instanceOf(responseType.expectedResponseType) as ResponseType<R>,
                    ResponseTypes.instanceOf(responseType.expectedResponseType) as ResponseType<R>
            )

            val queryResult: SubscriptionQueryResult<QueryResponseMessage<R>, SubscriptionQueryUpdateMessage<R>> = queryBus
                    .subscriptionQuery(
                            processInterceptors(subscriptionQueryMessage),
                            SubscriptionQueryBackpressure.defaultBackpressure(),
                            Queues.SMALL_BUFFER_SIZE
                    )

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
                    .map {
                        logger.debug { "REVISION-QUERY-GATEWAY-003: Response received:\n $it" }
                        it
                    }
                    .filter { it is Revisionable }
                    .map { it to (it as Revisionable).revisionValue }
                    .filter { pair -> pair.second.revision >= revisionQueryParameter.minimalRevision }
                    .map { pair ->
                        logger.debug { "REVISION-QUERY-GATEWAY-004: Responded $queryName having $revisionQueryParameter with revision ${pair.second}" }
                        pair.first
                    }
                    .timeout(
                            Duration.of(queryTimeout, ChronoUnit.SECONDS)
                    )
                    .onErrorResume { exception ->
                        Publisher {
                            result.completeExceptionally(exception)
                        }
                    }
                    .subscribe { projectionResult -> result.complete(projectionResult) }

            result
        }
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

// FIXME -> currently unused. remove...
//class QueryResponseMessageResponseType<T : Any> : AbstractResponseType<T> {
//
//  companion object {
//    @JvmStatic
//    inline fun <reified T : Any> queryResponseMessageResponseType() = QueryResponseMessageResponseType(T::class)
//  }
//
//  @JsonCreator
//  @ConstructorProperties("expectedResponseType")
//  constructor(@JsonProperty("expectedResponseType") clazz: KClass<T>) : super(clazz.java)
//
//  override fun matches(responseType: Type): Boolean {
//    val unwrapped = ReflectionUtils.unwrapIfType(responseType, QueryResponseMessage::class.java)
//    return isGenericAssignableFrom(unwrapped) || isAssignableFrom(unwrapped)
//  }
//
//  @SuppressWarnings("unchecked")
//  @Suppress("UNCHECKED_CAST")
//  override fun responseMessagePayloadType(): Class {
//    return expectedResponseType as Class<T>
//  }
//
//  @Suppress("UNCHECKED_CAST")
//  override fun forSerialization(): ResponseType<T> {
//    return ResponseTypes.instanceOf(expectedResponseType as Class<T>)
//  }
//
//  override fun convert(response: Any): T {
//    return super.convert(response)
//  }
//
//  override fun toString(): String {
//    return "QueryResponseMessageResponseType{$expectedResponseType}"
//  }
//
//
//}
