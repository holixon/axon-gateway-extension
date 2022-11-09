package io.holixon.axon.gateway.configuration.command

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 * Configuration properties to set the command dispatch strategy from the properties.
 */
@ConstructorBinding
@ConfigurationProperties("axon-gateway.command.dispatch-aware.strategy")
data class CommandDispatchStrategyProperties(
  val commandNames: Set<String> = setOf(),
  val commandPackages: Set<String> = setOf()
)
