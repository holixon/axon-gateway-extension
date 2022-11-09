package io.holixon.axon.gateway.command

import org.axonframework.commandhandling.CommandCallback
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.MessageHandler

/**
 * Strategy to decide how to register command handlers and dispatch commands.
 */
interface CommandDispatchStrategy {

  /**
   * Indicates whether the registration of the command handler should be performed on remote segment of the Axon Server bus.
   *
   * @param commandName name of the command to register the handler for.
   * @param messageHandler command handler to register.
   * @return if `true` the handler will be registered on both segments, otherwise on local segment only.
   */
  fun registerRemote(commandName: String, messageHandler: MessageHandler<in CommandMessage<*>?>): Boolean = true

  /**
   * Indicates whether the dispatch of the command should be performed on remote segment of the Axon Server bus.
   * @param commandMessage command message to dispatch.
   * @param commandCallback command callback to react on result.
   * @param C command type.
   * @param R result type.
   * @return if `true` the handler will be dispatched on remote segments, otherwise on local segment only.
   */
  fun <C : Any?, R : Any?> dispatchRemote(commandMessage: CommandMessage<C>, commandCallback: CommandCallback<in C, in R>): Boolean = true
}