package io.holixon.axon.gateway.configuration.command

import io.holixon.axon.gateway.command.CommandDispatchStrategy
import io.holixon.axon.gateway.command.strategy.CommandNamePredicateRegistrationCommandDispatchStrategy
import io.holixon.axon.gateway.command.strategy.CompositeOrRegistrationCommandDispatchStrategy
import io.holixon.axon.gateway.command.strategy.DelegatingToRegistrationCommandDispatchStrategy
import io.holixon.axon.gateway.command.strategy.MessageHandlerPredicateRegistrationCommandDispatchStrategy
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.MessageHandler
import org.axonframework.springboot.util.ConditionalOnMissingQualifiedBean
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import java.util.function.Predicate
import java.util.function.Predicate.not

/**
 * Configuration of the command dispatch strategy.
 */
@EnableConfigurationProperties(CommandDispatchStrategyProperties::class)
class CommandDispatchStrategyConfiguration {

  companion object {
    const val COMMAND_DISPATCH = "commandDispatch"
  }

  /**
   * Creates a default command dispatch strategy: delegating to registration OR-composite of
   * command-name based and message handler based strategies. The child strategies take into account
   * the predicates on Strings and MessageHandlers qualified with "commandDispatch"
   */
  @Bean
  @ConditionalOnMissingBean
  fun defaultCommandDispatchStrategy(
    @Qualifier(COMMAND_DISPATCH)
    commandNamePredicates: Set<Predicate<String>>,
    @Qualifier(COMMAND_DISPATCH)
    messageHandlerPredicates: Set<Predicate<MessageHandler<in CommandMessage<*>?>>>
  ): CommandDispatchStrategy {

    return DelegatingToRegistrationCommandDispatchStrategy(
      delegate = CompositeOrRegistrationCommandDispatchStrategy(
        childStrategies = setOf(
          CommandNamePredicateRegistrationCommandDispatchStrategy(commandNamePredicates),
          MessageHandlerPredicateRegistrationCommandDispatchStrategy(messageHandlerPredicates)
        )
      )
    )
  }

  /**
   * Provides property-based matchers, if no custom predicate is defined.
   * @param properties properties to construct predicates.
   * @return set of predicates for command names.
   */
  @Bean
  @ConditionalOnMissingQualifiedBean(beanClass = Predicate::class, qualifier = COMMAND_DISPATCH)
  @Qualifier(COMMAND_DISPATCH)
  fun propertyBasedCommandNamePredicates(properties: CommandDispatchStrategyProperties): Set<Predicate<String>> {
    val excludeCommandNames = properties.excludeCommandNames.map { commandName -> Predicate<String> { name -> commandName == name } }
    val excludeCommandPackages = properties.excludeCommandPackages.map { packageName -> Predicate<String> { name -> name.startsWith(packageName) } }
    val allExcludes = excludeCommandNames + excludeCommandPackages
    val any = Predicate<String> { value ->
      allExcludes.any { predicate -> predicate.test(value) }
    }
    return setOf(not(any))
  }


}