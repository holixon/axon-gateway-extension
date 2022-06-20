package io.holixon.axon.gateway.configuration.query

import org.springframework.context.annotation.Import

/**
 * Enables revision-aware query gateway
 */
@MustBeDocumented
@Import(RevisionAwareQueryGatewayConfiguration::class)
annotation class EnableRevisionAwareQueryGateway