package io.holixon.axon.gateway.query

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.axonframework.common.ReflectionUtils
import org.axonframework.messaging.MetaData
import org.axonframework.messaging.responsetypes.AbstractResponseType
import org.axonframework.messaging.responsetypes.ResponseType
import org.axonframework.queryhandling.GenericQueryResponseMessage
import org.axonframework.queryhandling.GenericSubscriptionQueryUpdateMessage
import org.axonframework.queryhandling.QueryResponseMessage
import org.axonframework.queryhandling.SubscriptionQueryUpdateMessage
import java.beans.ConstructorProperties
import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * Allows the query handlers to use [QueryResponseMessage] as response type
 * of the query handlers. This is helpful, if you want to control how the
 * message is constructed and what meta data gets in. See helper static methods for more details.
 */
class QueryResponseMessageResponseType<T : Any> : AbstractResponseType<T> {

  companion object {
    /**
     * Creates a response type for query response message. This allows your query handler to wrap the response into a query response message and provide some meta data.
     * @param [T] type of payload
     * @return query response message return type.
     */
    @JvmStatic
    inline fun <reified T : Any> queryResponseMessageResponseType() = QueryResponseMessageResponseType(T::class)

    /**
     * Creates a query response message with given payload.
     * @param [T] response payload type.
     * @param payload payload value.
     * @return message to be returned by the query handler.
     */
    @JvmStatic
    inline fun <reified T : Any?> asQueryResponseMessage(payload: Any?): QueryResponseMessage<T> = GenericQueryResponseMessage
      .asNullableResponseMessage(T::class.java, payload)

    /**
     * Creates a query response message with given payload and metadata.
     * @param [T] response payload type.
     * @param metaData meta data to be written into the message.
     * @param payload payload value.
     * @return message to be returned by the query handler.
     */
    @JvmStatic
    inline fun <reified T : Any?> asQueryResponseMessage(payload: Any?, metaData: MetaData): QueryResponseMessage<T> = GenericQueryResponseMessage
        .asNullableResponseMessage(T::class.java, payload)
        .andMetaData(metaData)

    /**
     * Creates a query subscription update message with given payload.
     * @param [T] response payload type.
     * @param payload payload value.
     * @return message to be passed to query update emitter.
     */
    @JvmStatic
    inline fun <reified T : Any?> asSubscriptionUpdateMessage(payload: Any?): SubscriptionQueryUpdateMessage<T> = GenericSubscriptionQueryUpdateMessage
        .asUpdateMessage(payload)

    /**
     * Creates a query subscription update message with given payload and metadata.
     * @param [T] response payload type.
     * @param metaData meta data to be written into the message.
     * @param payload payload value.
     * @return message to be passed to query update emitter.
     */
    @JvmStatic
    inline fun <reified T : Any> asSubscriptionUpdateMessage(payload: Any?, metaData: MetaData): SubscriptionQueryUpdateMessage<T> = GenericSubscriptionQueryUpdateMessage
        .asUpdateMessage<T>(payload)
        .andMetaData(metaData)


  }

  @JsonCreator
  @ConstructorProperties("expectedResponseType")
  constructor(@JsonProperty("expectedResponseType") clazz: KClass<T>) : super(clazz.java)

  override fun matches(responseType: Type): Boolean {
    val unwrapped = ReflectionUtils.unwrapIfType(responseType, QueryResponseMessage::class.java)
    return isGenericAssignableFrom(unwrapped) || isAssignableFrom(unwrapped)
  }

  @SuppressWarnings("unchecked")
  @Suppress("UNCHECKED_CAST")
  override fun responseMessagePayloadType(): Class<T> {
    return expectedResponseType as Class<T>
  }

  @Suppress("UNCHECKED_CAST")
  override fun forSerialization(): ResponseType<T> {
    return super.forSerialization() as ResponseType<T>
  }

  override fun toString(): String {
    return "QueryResponseMessageResponseType{$expectedResponseType}"
  }
}
