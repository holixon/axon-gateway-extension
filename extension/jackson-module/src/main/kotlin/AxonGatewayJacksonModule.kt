package io.holixon.axon.gateway.jackson.module

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * Module serializing the KClass correctly.
 * insired by https://github.com/FasterXML/jackson-module-kotlin/issues/361
 */
class AxonGatewayJacksonModule : SimpleModule() {
  init {
    addSerializer(KClass::class.java, KClassSerializer())
    addDeserializer(KClass::class.java, KClassDeserializer())
  }
}

/**
 * Serializes the KClass in writing the full classified class name into the payload JSON.
 */
class KClassSerializer : StdSerializer<KClass<*>>(KClass::class.java) {
  override fun serialize(value: KClass<*>, gen: JsonGenerator, provider: SerializerProvider) {
    gen.writeString(value.qualifiedName)
  }
}

/**
 * De-serializes the KClass in reading the full classified class name from the payload JSON.
 */
class KClassDeserializer : StdDeserializer<KClass<*>>(KClass::class.java) {

  companion object {
    /*
     * Static cache for classes.
     */
    private val cache: ConcurrentHashMap<String, KClass<*>> = ConcurrentHashMap()

    /**
     * Clears static class cache.
     */
    fun clearCache() {
      cache.clear()
    }
  }

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): KClass<*> {
    return cache.getOrPut(p.text) { Class.forName(p.text).kotlin }
  }
}
