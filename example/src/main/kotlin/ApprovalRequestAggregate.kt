package io.holixon.axon.gateway.example

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.messaging.MetaData
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
class ApprovalRequestAggregate() {

  @AggregateIdentifier
  lateinit var requestId: String

  @CommandHandler
  constructor(cmd: CreateApprovalRequestCommand, meta: MetaData) : this() {
    AggregateLifecycle.apply(ApprovalRequestCreatedEvent(
        requestId = cmd.requestId,
        subject = cmd.subject,
        amount = cmd.amount,
        currency = cmd.currency
    ), meta)
  }

  @CommandHandler
  fun handle(cmd: UpdateApprovalRequestCommand, meta: MetaData) {
    AggregateLifecycle.apply(ApprovalRequestUpdatedEvent(
        requestId = cmd.requestId,
        subject = cmd.subject,
        amount = cmd.amount,
        currency = cmd.currency
    ), meta)
  }

  @EventSourcingHandler
  fun on(evt: ApprovalRequestCreatedEvent) {
    requestId = evt.requestId
  }
}