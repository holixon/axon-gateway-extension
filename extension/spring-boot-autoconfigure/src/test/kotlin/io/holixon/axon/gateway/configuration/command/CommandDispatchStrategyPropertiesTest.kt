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
        "axon-gateway.command.dispatch-aware.strategy.command-names=foo.Name,bar.Other,zee.Third",
        "axon-gateway.command.dispatch-aware.strategy.command-packages=de.foo,il.bar,ua.zee",
      ).run {

        assertThat(it.getBean(CommandDispatchStrategyProperties::class.java)).isNotNull
        val props: CommandDispatchStrategyProperties = it.getBean(CommandDispatchStrategyProperties::class.java)

        assertThat(props.commandPackages).isEqualTo(setOf("de.foo", "il.bar", "ua.zee"))
        assertThat(props.commandNames).isEqualTo(setOf("foo.Name", "bar.Other", "zee.Third"))

        assertThat(it.getBean(CommandDispatchStrategy::class.java)).isNotNull
        val strategy = it.getBean(CommandDispatchStrategy::class.java)

        assertThat(strategy.registerRemote("de.foo.Some1") {}).isTrue
        assertThat(strategy.registerRemote("il.bar.Some2") {}).isTrue
        assertThat(strategy.registerRemote("ua.zee.Some3") {}).isTrue
        assertThat(strategy.registerRemote("foo.Name") {}).isTrue
        assertThat(strategy.registerRemote("bar.Other") {}).isTrue
        assertThat(strategy.registerRemote("zee.Third") {}).isTrue

        assertThat(strategy.registerRemote("random.Name") {}).isFalse
      }

  }


  @Test
  fun `construct predicates based configuration`() {
    contextRunner
      .withUserConfiguration(ManualPredicateConfiguration::class.java)
      .withPropertyValues(
        "axon-gateway.command.dispatch-aware.strategy.command-names=foo.Name,bar.Other,zee.Third",
        "axon-gateway.command.dispatch-aware.strategy.command-packages=de.foo,il.bar,ua.zee",
      ).run {

        assertThat(it.getBean(CommandDispatchStrategyProperties::class.java)).isNotNull
        val props: CommandDispatchStrategyProperties = it.getBean(CommandDispatchStrategyProperties::class.java)

        // properties are there
        assertThat(props.commandPackages).isEqualTo(setOf("de.foo", "il.bar", "ua.zee"))
        assertThat(props.commandNames).isEqualTo(setOf("foo.Name", "bar.Other", "zee.Third"))

        assertThat(it.getBean(CommandDispatchStrategy::class.java)).isNotNull
        val strategy = it.getBean(CommandDispatchStrategy::class.java)

        // but not used
        assertThat(strategy.registerRemote("de.foo.Some1") {}).isFalse
        assertThat(strategy.registerRemote("foo.Name") {}).isFalse

        // configured beans are used instead
        assertThat(strategy.registerRemote("manual") {}).isTrue
        assertThat(strategy.registerRemote("other") {}).isTrue

        assertThat(strategy.registerRemote("random.Name") {}).isFalse

      }
  }

}