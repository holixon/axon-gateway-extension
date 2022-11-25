package io.holixon.axon.gateway.configuration.command

import io.holixon.axon.gateway.command.CommandDispatchStrategy
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.context.annotation.UserConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

internal class CommandDispatchStrategyPropertiesTest {

  private val contextRunner = ApplicationContextRunner()
    .withConfiguration(UserConfigurations.of(CommandDispatchStrategyConfiguration::class.java))

  companion object {
    const val FOO = "foo.Name"
    const val BAR = "bar.Other"
    const val ZEE = "zee.Third"

    const val P1 = "de.foo"
    const val P2 = "il.bar"
    const val P3 = "ua.zee"
  }

  @Test
  fun `construct predicates based on properties`() {

    contextRunner

      .withPropertyValues(
        "axon-gateway.command.dispatch-aware.strategy.exclude-command-names=$FOO,$BAR,$ZEE",
        "axon-gateway.command.dispatch-aware.strategy.exclude-command-packages=$P1,$P2,$P3",
      ).run {

        assertThat(it.getBean(CommandDispatchStrategyProperties::class.java)).isNotNull
        val props: CommandDispatchStrategyProperties = it.getBean(CommandDispatchStrategyProperties::class.java)

        assertThat(props.excludeCommandNames).isEqualTo(setOf(FOO, BAR, ZEE))
        assertThat(props.excludeCommandPackages).isEqualTo(setOf(P1, P2, P3))

        assertThat(it.getBean(CommandDispatchStrategy::class.java)).isNotNull
        val strategy = it.getBean(CommandDispatchStrategy::class.java)

        assertThat(strategy.registerRemote("$P1.Some1") {}).isFalse
        assertThat(strategy.registerRemote("$P2.Some2") {}).isFalse
        assertThat(strategy.registerRemote("$P3.Some3") {}).isFalse
        assertThat(strategy.registerRemote(FOO) {}).isFalse
        assertThat(strategy.registerRemote(BAR) {}).isFalse
        assertThat(strategy.registerRemote(ZEE) {}).isFalse

        assertThat(strategy.registerRemote("random.Name") {}).isTrue
      }

  }


  @Test
  fun `construct predicates based configuration`() {
    contextRunner
      .withUserConfiguration(ManualPredicateConfiguration::class.java)
      .withPropertyValues(
        "axon-gateway.command.dispatch-aware.strategy.exclude-command-names=$FOO,$BAR,$ZEE",
        "axon-gateway.command.dispatch-aware.strategy.exclude-command-packages=$P1,$P2,$P3",
      ).run {

        assertThat(it.getBean(CommandDispatchStrategyProperties::class.java)).isNotNull
        val props: CommandDispatchStrategyProperties = it.getBean(CommandDispatchStrategyProperties::class.java)

        // properties are there
        assertThat(props.excludeCommandNames).isEqualTo(setOf(FOO, BAR, ZEE))
        assertThat(props.excludeCommandPackages).isEqualTo(setOf(P1, P2, P3))

        assertThat(it.getBean(CommandDispatchStrategy::class.java)).isNotNull
        val strategy = it.getBean(CommandDispatchStrategy::class.java)

        // configured beans are used instead
        assertThat(strategy.registerRemote(FOO) {}).isTrue // ignore the one from property
        assertThat(strategy.registerRemote("random.Name") {}).isFalse

      }
  }

}