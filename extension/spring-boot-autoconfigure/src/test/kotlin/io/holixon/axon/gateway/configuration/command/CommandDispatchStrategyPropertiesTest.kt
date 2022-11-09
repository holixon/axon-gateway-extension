package io.holixon.axon.gateway.configuration.command

import io.holixon.axon.gateway.command.CommandDispatchStrategy
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.context.annotation.UserConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

internal class CommandDispatchStrategyPropertiesTest {

  private val contextRunner = ApplicationContextRunner()
    .withConfiguration(UserConfigurations.of(CommandDispatchStrategyConfiguration::class.java))

  @Test
  fun `construct predicates based on properties`() {

    contextRunner

      .withPropertyValues(
        "axon-gateway.command.dispatch-aware.strategy.exclude-command-names=foo.Name,bar.Other,zee.Third",
        "axon-gateway.command.dispatch-aware.strategy.exclude-command-packages=de.foo,il.bar,ua.zee",
      ).run {

        assertThat(it.getBean(CommandDispatchStrategyProperties::class.java)).isNotNull
        val props: CommandDispatchStrategyProperties = it.getBean(CommandDispatchStrategyProperties::class.java)

        assertThat(props.excludeCommandPackages).isEqualTo(setOf("de.foo", "il.bar", "ua.zee"))
        assertThat(props.excludeCommandNames).isEqualTo(setOf("foo.Name", "bar.Other", "zee.Third"))

        assertThat(it.getBean(CommandDispatchStrategy::class.java)).isNotNull
        val strategy = it.getBean(CommandDispatchStrategy::class.java)

        assertThat(strategy.registerRemote("de.foo.Some1") {}).isFalse
        assertThat(strategy.registerRemote("il.bar.Some2") {}).isFalse
        assertThat(strategy.registerRemote("ua.zee.Some3") {}).isFalse
        assertThat(strategy.registerRemote("foo.Name") {}).isFalse
        assertThat(strategy.registerRemote("bar.Other") {}).isFalse
        assertThat(strategy.registerRemote("zee.Third") {}).isFalse

        assertThat(strategy.registerRemote("random.Name") {}).isTrue
      }

  }


  @Test
  fun `construct predicates based configuration`() {
    contextRunner
      .withUserConfiguration(ManualPredicateConfiguration::class.java)
      .withPropertyValues(
        "axon-gateway.command.dispatch-aware.strategy.exclude-command-names=foo.Name,bar.Other,zee.Third",
        "axon-gateway.command.dispatch-aware.strategy.exclude-command-packages=de.foo,il.bar,ua.zee",
      ).run {

        assertThat(it.getBean(CommandDispatchStrategyProperties::class.java)).isNotNull
        val props: CommandDispatchStrategyProperties = it.getBean(CommandDispatchStrategyProperties::class.java)

        // properties are there
        assertThat(props.excludeCommandPackages).isEqualTo(setOf("de.foo", "il.bar", "ua.zee"))
        assertThat(props.excludeCommandNames).isEqualTo(setOf("foo.Name", "bar.Other", "zee.Third"))

        assertThat(it.getBean(CommandDispatchStrategy::class.java)).isNotNull
        val strategy = it.getBean(CommandDispatchStrategy::class.java)

        // configured beans are used instead
        assertThat(strategy.registerRemote("foo.Name") {}).isTrue // ignore the one from property

        assertThat(strategy.registerRemote("random.Name") {}).isFalse

      }
  }

}