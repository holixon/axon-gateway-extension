package io.holixon.axon.gateway.command.strategy

import io.holixon.axon.gateway.command.CommandDispatchStrategy
import org.axonframework.commandhandling.CommandCallback
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.MessageHandler

/**
 * Command dispatch strategy delegating the registration decision to another strategy.
 * It decides about the command dispatch based on command name and the decision about the registration.
 */
class DelegatingToRegistrationCommandDispatchStrategy(
  private val delegate: CommandDispatchStrategy
) : CommandDispatchStrategy {

  private val remoteCommands: MutableSet<String> = mutableSetOf()

  override fun registerRemote(commandName: String, messageHandler: MessageHandler<in CommandMessage<*>?>): Boolean {
    return delegate.registerRemote(commandName, messageHandler).also { delegateDecision ->
      if (delegateDecision) {
        // the delegate strategy decided to handle remote. Remember this to use this information during dispatch.
        remoteCommands.add(commandName)
      }
    }
  }

  override fun <C, R> dispatchRemote(commandMessage: CommandMessage<C>, commandCallback: CommandCallback<in C, in R>): Boolean {
    return remoteCommands.contains(commandMessage.commandName)
  }
}