package io.holixon.axon.gateway.configuration.command

import io.holixon.axon.gateway.command.DispatchAwareAxonServerCommandBus
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.CommandBus
import org.axonframework.commandhandling.SimpleCommandBus
import org.axonframework.springboot.autoconfig.*
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

internal class DispatchAwareAxonServerCommandBusConfigurationTest {

  private val contextRunner = ApplicationContextRunner()
    // spring.factories in axon-spring-boot-autoconfigure
    .withConfiguration(AutoConfigurations.of(AxonTracingAutoConfiguration::class.java))
    .withConfiguration(AutoConfigurations.of(EventProcessingAutoConfiguration::class.java))
    .withConfiguration(AutoConfigurations.of(AxonAutoConfiguration::class.java))
    .withConfiguration(AutoConfigurations.of(NoOpTransactionAutoConfiguration::class.java))
    .withConfiguration(AutoConfigurations.of(InfraConfiguration::class.java))
    .withConfiguration(AutoConfigurations.of(AxonServerAutoConfiguration::class.java))
    .withConfiguration(AutoConfigurations.of(XStreamAutoConfiguration::class.java))
    .withConfiguration(AutoConfigurations.of(CommandDispatchStrategyConfiguration::class.java))
    .withConfiguration(AutoConfigurations.of(DispatchAwareAxonServerCommandBusConfiguration::class.java))

  @Test
  fun `construct dispatch aware axon server bus`() {
    contextRunner
      .withPropertyValues(
        "axon.axonserver.enabled=true",
        "axon-gateway.command.dispatch-aware.enabled=true",
        "axon-gateway.command.dispatch-aware.strategy.command-packages=de.foo,il.bar,ua.zee",
      ).run {

        assertThat(it.getBean(CommandDispatchStrategyProperties::class.java)).isNotNull
        val props: CommandDispatchStrategyProperties = it.getBean(CommandDispatchStrategyProperties::class.java)

        assertThat(props.commandPackages).isEqualTo(setOf("de.foo", "il.bar", "ua.zee"))

        assertThat(it.getBean(CommandBus::class.java)).isNotNull
        val bus = it.getBean(CommandBus::class.java)

        assertThat(bus).isInstanceOf(DispatchAwareAxonServerCommandBus::class.java)
      }
  }

  @Test
  fun `construct local bus if disabled by property`() {
    contextRunner
      .withPropertyValues(
        "axon.axonserver.enabled=true",
        "axon-gateway.command.dispatch-aware.enabled=false",
        "axon-gateway.command.dispatch-aware.strategy.command-packages=de.foo,il.bar,ua.zee",
      ).run {

        assertThat(it.getBean(CommandDispatchStrategyProperties::class.java)).isNotNull
        val props: CommandDispatchStrategyProperties = it.getBean(CommandDispatchStrategyProperties::class.java)

        assertThat(props.commandPackages).isEqualTo(setOf("de.foo", "il.bar", "ua.zee"))

        assertThat(it.getBean(CommandBus::class.java)).isNotNull
        val bus = it.getBean(CommandBus::class.java)

        assertThat(bus).isInstanceOf(SimpleCommandBus::class.java)
      }
  }
}

