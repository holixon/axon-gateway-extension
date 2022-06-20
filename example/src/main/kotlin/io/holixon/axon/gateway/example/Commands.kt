package io.holixon.axon.gateway.example

import org.axonframework.modelling.command.TargetAggregateIdentifier

/**
 * Create approval request.
 */
data class CreateApprovalRequestCommand(
    @TargetAggregateIdentifier
    val requestId: String,
    val subject: String,
    val amount: String,
    val currency: String
)

/**
 * Update approval request.
 */
data class UpdateApprovalRequestCommand(
    @TargetAggregateIdentifier
    val requestId: String,
    val subject: String,
    val amount: String,
    val currency: String
)