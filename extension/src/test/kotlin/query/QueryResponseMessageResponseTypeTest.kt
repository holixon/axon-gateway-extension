package io.holixon.axon.gateway.query

import com.fasterxml.jackson.module.kotlin.readValue
import io.holixon.axon.gateway.Foo
import io.holixon.axon.gateway.TestFixtures.OM
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.queryhandling.QueryResponseMessage
import org.junit.jupiter.api.Test


internal class QueryResponseMessageResponseTypeTest {


  @Test
  fun `serialize and deserialize to jackson`() {
    val responseType: QueryResponseMessageResponseType<Foo> = QueryResponseMessageResponseType.queryResponseMessageResponseType()

    val json = OM.writeValueAsString(responseType)


    val deserialized = OM.readValue<QueryResponseMessageResponseType<Foo>>(json)

    assertThat(deserialized).isEqualTo(responseType)
  }
}