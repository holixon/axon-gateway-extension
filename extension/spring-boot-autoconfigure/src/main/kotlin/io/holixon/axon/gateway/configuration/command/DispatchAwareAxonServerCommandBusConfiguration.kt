package io.holixon.axon.gateway.configuration.command

import io.holixon.axon.gateway.command.CommandDispatchStrategy
import io.holixon.axon.gateway.command.DispatchAwareAxonServerCommandBus
import org.axonframework.axonserver.connector.AxonServerConfiguration
import org.axonframework.axonserver.connector.AxonServerConnectionManager
import org.axonframework.axonserver.connector.TargetContextResolver
import org.axonframework.axonserver.connector.command.AxonServerCommandBus
import org.axonframework.axonserver.connector.command.CommandLoadFactorProvider
import org.axonframework.axonserver.connector.command.CommandPriorityCalculator
import org.axonframework.commandhandling.CommandBus
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.commandhandling.distributed.RoutingStrategy
import org.axonframework.serialization.Serializer
import org.axonframework.springboot.autoconfig.AxonServerBusAutoConfiguration
import org.axonframework.springboot.util.ConditionalOnMissingQualifiedBean
import org.axonframework.tracing.SpanFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary

/**
 * Configuration replacing default AxonServerBusAutoConfiguration allowing control on registration and dispatch
 * of commands based on [CommandDispatchStrategy].
 */
// inherit the remaining annotations from super class
@AutoConfigureAfter(CommandDispatchStrategyConfiguration::class)
// enabled only if the axon-server is enabled and the dispatch aware gateway is enabled.
// respects the default for `axon.axonserver.enabled` enablement (`true` if absent)
// sets the default of `axon-gateway.command.dispatch-aware.enabled` to `false`
@ConditionalOnExpression("#{\${axon.axonserver.enabled:true} and \${axon-gateway.command.dispatch-aware.enabled:false}}")
class DispatchAwareAxonServerCommandBusConfiguration(
  private val commandDispatchStrategy: CommandDispatchStrategy
) : AxonServerBusAutoConfiguration() {

  @Bean
  @Primary
  @ConditionalOnMissingQualifiedBean(qualifier = "!localSegment", beanClass = CommandBus::class)
  override fun axonServerCommandBus(
    axonServerConnectionManager: AxonServerConnectionManager,
    axonServerConfiguration: AxonServerConfiguration,
    @Qualifier("localSegment") localSegment: CommandBus,
    @Qualifier("messageSerializer") messageSerializer: Serializer,
    routingStrategy: RoutingStrategy?,
    priorityCalculator: CommandPriorityCalculator?,
    loadFactorProvider: CommandLoadFactorProvider?,
    targetContextResolver: TargetContextResolver<in CommandMessage<*>?>?,
    spanFactory: SpanFactory
  ): AxonServerCommandBus {
    return DispatchAwareAxonServerCommandBus(
      builder = AxonServerCommandBus
        .builder()
        .axonServerConnectionManager(axonServerConnectionManager)
        .configuration(axonServerConfiguration)
        .localSegment(localSegment)
        .serializer(messageSerializer)
        .routingStrategy(routingStrategy)
        .priorityCalculator(priorityCalculator)
        .loadFactorProvider(loadFactorProvider)
        .targetContextResolver(targetContextResolver)
        .spanFactory(spanFactory),
      commandDispatchStrategy = commandDispatchStrategy
    )
  }

}