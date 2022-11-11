package io.holixon.axon.gateway.command.strategy

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.MessageHandler
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.function.Predicate

internal class CommandNamePredicateRegistrationCommandDispatchStrategyTest {

  @Test
  fun `delegates to predicate if present`() {

    assertFalse(CommandNamePredicateRegistrationCommandDispatchStrategy(setOf()).registerRemote("any") {})

    val predicate = mockk<Predicate<String>>()
    every { predicate.test(any()) }.returns(true)

    val strategy = CommandNamePredicateRegistrationCommandDispatchStrategy(setOf(
      predicate
    ))
    assertTrue(strategy.registerRemote("any") {})

    verify {
      predicate.test(any())
    }

  }
}