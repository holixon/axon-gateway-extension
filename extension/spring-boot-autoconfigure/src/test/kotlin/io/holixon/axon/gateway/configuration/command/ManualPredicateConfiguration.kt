package io.holixon.axon.gateway.configuration.command

import io.holixon.axon.gateway.configuration.command.CommandDispatchStrategyConfiguration.Companion.COMMAND_DISPATCH
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import java.util.function.Predicate

/**
 * Example of manual config.
 */
class ManualPredicateConfiguration {


  /**
   * For example a predicate on a name.
   */
  @Bean
  @Qualifier(COMMAND_DISPATCH)
  fun otherCommandPredicate(): Predicate<String> = Predicate<String> { name -> name == "foo.Name" }

}