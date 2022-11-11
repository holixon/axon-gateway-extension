package io.holixon.axon.gateway.command.strategy

import io.holixon.axon.gateway.command.CommandDispatchStrategy
import io.mockk.every
import io.mockk.mockk
import org.axonframework.commandhandling.CommandCallback
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.messaging.GenericMessage
import org.axonframework.messaging.MetaData
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class DelegatingToRegistrationCommandDispatchStrategyTest {

  companion object {
    private const val REMOTE = "remote"
    private const val LOCAL = "local"
  }

  @Test
  fun `delegates dispatch to registration decision`() {

    val registerStrategy: CommandDispatchStrategy = mockk()
    every { registerStrategy.registerRemote(REMOTE, any()) }.returns(true)
    every { registerStrategy.registerRemote(LOCAL, any()) }.returns(false)

    val strategy = DelegatingToRegistrationCommandDispatchStrategy(
      registerStrategy
    )

    assertTrue(strategy.registerRemote(REMOTE) {}, "Should register remote")
    assertFalse(strategy.registerRemote(LOCAL) {}, "Should register remote")

    assertTrue(strategy.dispatchRemote(TestCommand().asNamedCommand(REMOTE), CommandCallback<TestCommand, Void> { _, _ -> }))
    assertFalse(strategy.dispatchRemote(TestCommand().asNamedCommand(LOCAL), CommandCallback<TestCommand, Void> { _, _ -> }))
  }

  data class TestCommand(
    val id: String = UUID.randomUUID().toString()
  ) {
    fun asNamedCommand(name: String) = GenericCommandMessage(GenericMessage(this, MetaData.emptyInstance()), name)
  }
}