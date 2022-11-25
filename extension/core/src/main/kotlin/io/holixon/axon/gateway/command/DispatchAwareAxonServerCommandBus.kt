package io.holixon.axon.gateway.command

import org.axonframework.axonserver.connector.command.AxonServerCommandBus
import org.axonframework.axonserver.connector.command.AxonServerRegistration
import org.axonframework.commandhandling.CommandCallback
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.common.Registration
import org.axonframework.messaging.MessageHandler
import org.slf4j.LoggerFactory

/**
 * Command bus taking in account dispatching strategy during registration of command handlers and dispatching of commands.
 */
open class DispatchAwareAxonServerCommandBus(
  builder: Builder,
  private val commandDispatchStrategy: CommandDispatchStrategy
) : AxonServerCommandBus(builder) {

  companion object {
    @JvmStatic
    private val logger = LoggerFactory.getLogger(DispatchAwareAxonServerCommandBus::class.java)
  }

  override fun subscribe(commandName: String, messageHandler: MessageHandler<in CommandMessage<*>?>): Registration {
    return if (commandDispatchStrategy.registerRemote(commandName = commandName, messageHandler = messageHandler)) {
      super.subscribe(commandName, messageHandler)
    } else {
      logger.info("DISPATCH-AWARE-COMMAND_GATEWAY-002: Subscribing command with name [{}] to local command bus only.", commandName)
      return AxonServerRegistration(localSegment().subscribe(commandName, messageHandler)) { }
    }
  }

  override fun <C : Any?, R : Any?> dispatch(commandMessage: CommandMessage<C>, commandCallback: CommandCallback<in C, in R>) {
    return if (commandDispatchStrategy.dispatchRemote(commandMessage = commandMessage, commandCallback = commandCallback)) {
      super.dispatch(commandMessage, commandCallback)
    } else {
      logger.debug("DISPATCH-AWARE-COMMAND_GATEWAY-003: Dispatching command [{}] with callback to local command bus only.", commandMessage.commandName)
      localSegment().dispatch(commandMessage, commandCallback)
    }
  }
}