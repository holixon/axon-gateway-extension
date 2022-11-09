package io.holixon.axon.gateway.command.strategy

import io.holixon.axon.gateway.command.CommandDispatchStrategy
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.MessageHandler

/**
 * Command dispatch strategy responsible for registration of commands only and
 * making this decision by applying a logical OR to the decisions made by provided child strategies.
 * If the list of child strategies, remote registration delivers true.
 */
class CompositeOrRegistrationCommandDispatchStrategy(
  private val childStrategies: Set<CommandDispatchStrategy>
) : CommandDispatchStrategy {

  override fun registerRemote(commandName: String, messageHandler: MessageHandler<in CommandMessage<*>?>): Boolean {
    return if (childStrategies.isEmpty()) {
      super.registerRemote(commandName, messageHandler)
    } else {
      childStrategies.any { child -> child.registerRemote(commandName, messageHandler) }
    }
  }
}