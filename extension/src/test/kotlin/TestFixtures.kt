package io.holixon.axon.gateway

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object TestFixtures {

  val OM = jacksonObjectMapper()
    .registerModule(JavaTimeModule())

}

data class Foo(val name:String)