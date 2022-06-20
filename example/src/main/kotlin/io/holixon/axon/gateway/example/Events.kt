package io.holixon.axon.gateway.example

import org.axonframework.serialization.Revision

/**
 * Created.
 */
@Revision("1")
data class ApprovalRequestCreatedEvent(
    val requestId: String,
    val subject: String,
    val amount: String,
    val currency: String
)

/**
 * Updated.
 */
@Revision("1")
data class ApprovalRequestUpdatedEvent(
    val requestId: String,
    val subject: String,
    val amount: String,
    val currency: String
)