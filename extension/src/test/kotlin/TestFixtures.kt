package io.holixon.axon.gateway

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlin.reflect.KClass

object TestFixtures {

  val OM = jacksonObjectMapper()
    .registerModule(JavaTimeModule())
    .registerModule(ExtendedKotlinModule())

}

class ExtendedKotlinModule : SimpleModule() {
  init {
    addSerializer(KClass::class.java, KClassSerializer())
    addDeserializer(KClass::class.java, KClassDeserializer())
  }
}

class KClassSerializer : StdSerializer<KClass<*>>(KClass::class.java) {
  override fun serialize(value: KClass<*>, gen: JsonGenerator, provider: SerializerProvider) {
    gen.writeString(value.qualifiedName)
  }
}

class KClassDeserializer : StdDeserializer<KClass<*>>(KClass::class.java) {
  override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): KClass<*> {
    val qualifiedName = p.text
    return Class.forName(qualifiedName).kotlin
  }
}

data class Foo(val name:String)