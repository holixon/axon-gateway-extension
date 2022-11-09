package io.holixon.axon.gateway.configuration.command

import io.holixon.axon.gateway.configuration.command.CommandDispatchStrategyConfiguration.Companion.COMMAND_DISPATCH
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import java.util.function.Predicate

class ManualPredicateConfiguration {


  @Bean
  @Qualifier(COMMAND_DISPATCH)
  fun otherCommandPredicate(): Predicate<String> = Predicate<String> { name -> name == "foo.Name" }

}