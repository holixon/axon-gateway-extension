package io.holixon.axon.gateway.jackson.module

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.holixon.axon.gateway.query.QueryResponseMessageResponseType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class QueryResponseMessageResponseTypeTest {

  private val objectMapper: ObjectMapper = jacksonObjectMapper()
    .registerModule(AxonGatewayJacksonModule())


  @Test
  fun `serialize and deserialize simple type using jackson`() {

    val responseType: QueryResponseMessageResponseType<Foo> =
      QueryResponseMessageResponseType.queryResponseMessageResponseType()
    val json = objectMapper.writeValueAsString(responseType)
    val deserialized = objectMapper.readValue<QueryResponseMessageResponseType<Foo>>(json)

    assertEquals(deserialized, responseType)
  }


  @Test
  fun `serialize and deserialize collection type using jackson`() {

    val responseType: QueryResponseMessageResponseType<List<Foo>> =
      QueryResponseMessageResponseType.queryResponseMessageResponseType()
    val json = objectMapper.writeValueAsString(responseType)
    val deserialized = objectMapper.readValue<QueryResponseMessageResponseType<List<Foo>>>(json)

    assertEquals(deserialized, responseType)
  }

}

data class Foo(val name: String)