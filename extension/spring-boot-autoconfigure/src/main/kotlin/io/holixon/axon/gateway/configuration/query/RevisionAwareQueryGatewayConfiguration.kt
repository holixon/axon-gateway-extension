package io.holixon.axon.gateway.configuration.query

import io.holixon.axon.gateway.query.RevisionAwareQueryGateway
import org.axonframework.queryhandling.QueryBus
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.springboot.autoconfig.AxonAutoConfiguration
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Activates the revision-aware query gateway.
 */
@Configuration
@EnableConfigurationProperties(RevisionAwareQueryGatewayProperties::class)
@ConditionalOnProperty(prefix = "axon-gateway.query", name = ["type"], havingValue = "revision-aware")
@AutoConfigureBefore(AxonAutoConfiguration::class)
class RevisionAwareQueryGatewayConfiguration {

  companion object {
    @JvmStatic
    private val logger = LoggerFactory.getLogger(RevisionAwareQueryGatewayConfiguration::class.java)
  }

  /**
   * Factory function creating a revision-aware query gateway.
   */
  @Bean
  fun revisionAwareGateway(queryBus: QueryBus, properties: RevisionAwareQueryGatewayProperties): QueryGateway {
    logger.info("REVISION-QUERY-GATEWAY-001: Using revision-aware query gateway.")
    return RevisionAwareQueryGateway(queryBus, properties.defaultQueryTimeout)
  }
}