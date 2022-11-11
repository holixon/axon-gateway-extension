package io.holixon.axon.gateway.command.strategy

import io.holixon.axon.gateway.command.CommandDispatchStrategy
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.MessageHandler
import java.util.function.Predicate

/**
 * Command dispatch strategy responsible for registration of commands only and
 * making this decision depending on command name.
 */
class CommandNamePredicateRegistrationCommandDispatchStrategy(
  private val predicates: Set<Predicate<String>> = setOf()
): CommandDispatchStrategy {

  override fun registerRemote(commandName: String, messageHandler: MessageHandler<in CommandMessage<*>?>): Boolean {
    return predicates.any { it.test(commandName) }
  }
}