package io.holixon.axon.gateway.command.strategy

import io.holixon.axon.gateway.command.CommandDispatchStrategy
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.MessageHandler
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CompositeAndRegistrationCommandDispatchStrategyTest {

  private val rejectingStrategy = object : CommandDispatchStrategy {
    override fun registerRemote(commandName: String, messageHandler: MessageHandler<in CommandMessage<*>?>): Boolean = false
  }

  private val acceptingStrategy = object : CommandDispatchStrategy {}

  @Test
  fun `delegates to children applying OR`() {
    assertTrue(
      CompositeAndRegistrationCommandDispatchStrategy(
        setOf(acceptingStrategy, acceptingStrategy, acceptingStrategy)
      ).registerRemote("any") {},
    )

    assertFalse(
      CompositeAndRegistrationCommandDispatchStrategy(
        setOf(acceptingStrategy, acceptingStrategy, rejectingStrategy)
      ).registerRemote("any") {},
    )

    assertFalse(
      CompositeAndRegistrationCommandDispatchStrategy(
        setOf(rejectingStrategy, rejectingStrategy, rejectingStrategy)
      ).registerRemote("any") {},
    )

    assertTrue(
      CompositeAndRegistrationCommandDispatchStrategy(
        setOf()
      ).registerRemote("any") {},
    )
  }

}