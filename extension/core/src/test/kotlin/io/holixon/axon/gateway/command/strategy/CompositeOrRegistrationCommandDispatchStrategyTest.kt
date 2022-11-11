package io.holixon.axon.gateway.command.strategy

import io.holixon.axon.gateway.command.CommandDispatchStrategy
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.MessageHandler
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CompositeOrRegistrationCommandDispatchStrategyTest {

  private val rejectingStrategy = object : CommandDispatchStrategy {
    override fun registerRemote(commandName: String, messageHandler: MessageHandler<in CommandMessage<*>?>): Boolean = false
  }

  private val acceptingStrategy = object : CommandDispatchStrategy {}

  @Test
  fun `delegates to children applying OR`() {
    assertTrue(
      CompositeOrRegistrationCommandDispatchStrategy(
        setOf(acceptingStrategy, acceptingStrategy, acceptingStrategy)
      ).registerRemote("any") {},
    )

    assertTrue(
      CompositeOrRegistrationCommandDispatchStrategy(
        setOf(acceptingStrategy, acceptingStrategy, rejectingStrategy)
      ).registerRemote("any") {},
    )

    assertFalse(
      CompositeOrRegistrationCommandDispatchStrategy(
        setOf(rejectingStrategy, rejectingStrategy, rejectingStrategy)
      ).registerRemote("any") {},
    )

    assertTrue(
      CompositeOrRegistrationCommandDispatchStrategy(
        setOf()
      ).registerRemote("any") {},
    )
  }

}