package io.holixon.axon.gateway.query

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
