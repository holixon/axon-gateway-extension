package io.holixon.axon.gateway.configuration.query

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 * Properties of the revision-aware query gateway.
 * @param defaultQueryTimeout default timeout in seconds to wait until the query delivers a
 * timeout, if not specified in the query. If not set, the default of 3 seconds is used.
 * <code>
 * axon-gateway:
 *   query:
 *       type: revision-aware
 *       revision-aware:
 *           default-query-timeout: 10
 * </code>
 */
@ConfigurationProperties("axon-gateway.query.revision-aware")
@ConstructorBinding
data class RevisionAwareQueryGatewayProperties(
    val defaultQueryTimeout: Long = 3
)