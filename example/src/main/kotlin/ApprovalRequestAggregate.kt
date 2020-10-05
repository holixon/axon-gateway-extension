package io.holixon.axon.gateway.example

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate

/**
 * Aggregate root responsible for approval request handling.
 */
@Aggregate
class ApprovalRequestAggregate() {

  @AggregateIdentifier
  lateinit var requestId: String

  /**
   * Create new aggregate for this request.
   * @param cmd Creation command.
   * @param meta command metadata.
   */
  @CommandHandler
  constructor(cmd: CreateApprovalRequestCommand) : this() {
    AggregateLifecycle.apply(ApprovalRequestCreatedEvent(
        requestId = cmd.requestId,
        subject = cmd.subject,
        amount = cmd.amount,
        currency = cmd.currency
    ))
  }

  /**
   * Updates aggregate for this request.
   * @param cmd Update command.
   * @param meta command metadata.
   */
  @CommandHandler
  fun handle(cmd: UpdateApprovalRequestCommand) {
    AggregateLifecycle.apply(ApprovalRequestUpdatedEvent(
        requestId = cmd.requestId,
        subject = cmd.subject,
        amount = cmd.amount,
        currency = cmd.currency
    ))
  }

  /**
   * React on creation and store the aggregate id.
   * @param evt sourcing event.
   */
  @EventSourcingHandler
  fun on(evt: ApprovalRequestCreatedEvent) {
    requestId = evt.requestId
  }
}