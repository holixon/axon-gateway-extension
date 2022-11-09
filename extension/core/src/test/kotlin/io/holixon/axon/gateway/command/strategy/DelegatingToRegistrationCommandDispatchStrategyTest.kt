package io.holixon.axon.gateway.command.strategy

import io.holixon.axon.gateway.command.CommandDispatchStrategy
import io.mockk.every
import io.mockk.mockk
import org.axonframework.commandhandling.CommandCallback
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.messaging.GenericMessage
import org.axonframework.messaging.MetaData
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class DelegatingToRegistrationCommandDispatchStrategyTest {

  @Test
  fun `delegates dispatch to registration decision`() {

    val registerStrategy: CommandDispatchStrategy = mockk()
    every { registerStrategy.registerRemote("remote", any()) }.returns(true)
    every { registerStrategy.registerRemote("local", any()) }.returns(false)

    val strategy = DelegatingToRegistrationCommandDispatchStrategy(
      registerStrategy
    )

    assertTrue(strategy.registerRemote("remote") {}, "Should register remote")
    assertFalse(strategy.registerRemote("local") {}, "Should register remote")

    assertTrue(strategy.dispatchRemote(TestCommand("id1").asNamedCommand("remote"), CommandCallback<TestCommand, Void> { _, _ -> }))
    assertFalse(strategy.dispatchRemote(TestCommand("id1").asNamedCommand("local"), CommandCallback<TestCommand, Void> { _, _ -> }))
  }

  data class TestCommand(
    val id: String
  ) {
    fun asNamedCommand(name: String) = GenericCommandMessage(GenericMessage(this, MetaData.emptyInstance()), name)
  }
}