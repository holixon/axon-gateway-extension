package io.holixon.axon.gateway.configuration.query

import org.springframework.context.annotation.Import

/**
 * Enables revision-aware query gateway
 */
@Deprecated(message = "Replaced b autoconfiguration of the module and property.")
@MustBeDocumented
@Import(RevisionAwareQueryGatewayConfiguration::class)
annotation class EnableRevisionAwareQueryGateway