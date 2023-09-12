package io.holixon.axon.gateway.configuration.command

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties to set the command dispatch strategy from the properties.
 */
@ConfigurationProperties("axon-gateway.command.dispatch-aware.strategy")
data class CommandDispatchStrategyProperties(
  val excludeCommandNames: Set<String> = setOf(),
  val excludeCommandPackages: Set<String> = setOf()
)
