package io.holixon.axon.gateway.example

import io.holixon.axon.gateway.query.QueryResponseMessageResponseType
import io.holixon.axon.gateway.query.RevisionValue
import io.holixon.axon.gateway.query.Revisionable
import org.axonframework.eventhandling.EventHandler
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryResponseMessage
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory storage for approval requests.
 */
@Component
class ApprovalRequestProjection(
  private val queryUpdateEmitter: QueryUpdateEmitter
) {

  companion object {
    @JvmStatic
    val logger: Logger = LoggerFactory.getLogger(ApprovalRequestProjection::class.java)
  }

  private val storage: ConcurrentHashMap<String, ApprovalRequest> = ConcurrentHashMap()
  private val revisions: ConcurrentHashMap<String, RevisionValue> = ConcurrentHashMap()

  /**
   * Query handler.
   * @param query a query to retrieve an approval request by id.
   * @return wrapper result containing the approval request.
   */
  @QueryHandler
  fun getOneInResult(query: ApprovalRequestQuery): ApprovalRequestQueryResult {
    logger.info("Query one for ${query.requestId} with revision info in result.")
    val payload = storage[query.requestId]
    val revision = revisions[query.requestId] ?: RevisionValue.NO_REVISION
    logger.info("Found payload: `$payload`, revision `$revision`")
    return ApprovalRequestQueryResult(payload = payload, revisionValue = revision)
  }

  /**
   * Query handler.
   * @param query a query to retrieve an approval request by id.
   * @return approval request.
   */
  @QueryHandler
  fun getOne(query: ApprovalRequestQuery): QueryResponseMessage<ApprovalRequest?> {
    logger.info("Query one for ${query.requestId} with revision in metadata.")
    val payload = storage[query.requestId]
    val revision = revisions[query.requestId] ?: RevisionValue.NO_REVISION
    logger.info("Found payload: `$payload`, revision `$revision`")
    return QueryResponseMessageResponseType.asQueryResponseMessage(payload, revision.toMetaData())
  }

  /**
   * Handler for create.
   * @param evt create event.
   * @param meta metadata of the event.
   */
  @EventHandler
  fun on(evt: ApprovalRequestCreatedEvent, meta: MetaData) {
    // overwrite with version from events
    logger.info("Received created for ${evt.requestId} with revision ${RevisionValue.fromMetaData(meta).revision}")
    revisions[evt.requestId] = RevisionValue.fromMetaData(meta)
    this.storage[evt.requestId] = ApprovalRequest(
      requestId = evt.requestId,
      subject = evt.subject,
      amount = evt.amount,
      currency = evt.currency
    )
    updateSubscriptions(evt.requestId)
  }

  /**
   * Handler for update.
   * @param evt update event.
   * @param meta metadata of the event.
   */
  @EventHandler
  fun on(evt: ApprovalRequestUpdatedEvent, meta: MetaData) {
    logger.info("Received updated for ${evt.requestId} with revision ${RevisionValue.fromMetaData(meta).revision}")
    if (checkAndUpdateRevision(evt.requestId, RevisionValue.fromMetaData(meta))) {
      this.storage[evt.requestId] = ApprovalRequest(
        requestId = evt.requestId,
        subject = evt.subject,
        amount = evt.amount,
        currency = evt.currency
      )
      updateSubscriptions(evt.requestId)
    } else {
      logger.warn("Skipping update, new event should have a higher revision than existing.")
    }
  }

  /**
   * Checks and updates revision.
   * @param requestId id of the request to send the update about.
   * @param fromMetaData revision from metadata of the event.
   * @return true, if the revision has been changed, false otherwise.
   */
  private fun checkAndUpdateRevision(requestId: String, fromMetaData: RevisionValue): Boolean {
    val old = revisions.getValue(requestId)
    return if (fromMetaData > old) { // increment only
      revisions[requestId] = fromMetaData
      true
    } else {
      false
    }
  }

  /**
   * Updates subscriptions.
   * @param requestId id of the request to send the update about.
   */
  private fun updateSubscriptions(requestId: String) {

    logger.info("Emitting update for $requestId")
    // update queries for QueryResponseMessageResponseType
    queryUpdateEmitter.emit(
      ApprovalRequestQuery::class.java,
      { query -> query.requestId == requestId },
      QueryResponseMessageResponseType.asSubscriptionUpdateMessage(payload = storage.getValue(requestId), metaData = revisions.getValue(requestId).toMetaData())
    )

    // update queries for InstanceResponseType
    queryUpdateEmitter.emit(
      ApprovalRequestQuery::class.java,
      { query -> query.requestId == requestId },
      ApprovalRequestQueryResult(payload = storage.getValue(requestId), revisionValue = revisions.getValue(requestId))
    )
  }
}

/**
 * View model of the approval request.
 * @param requestId unique request id.
 * @param subject approval subject
 * @param amount amount to approve.
 * @param currency currency.
 */
data class ApprovalRequest(
  val requestId: String,
  val subject: String,
  val amount: String,
  val currency: String
)

/**
 * Query for approval request by id.
 * @param requestId request id to query for.
 */
data class ApprovalRequestQuery(
  val requestId: String
)

/**
 * Query result.
 * @param payload actual data entry.
 * @param revision of the projection.
 */
data class ApprovalRequestQueryResult(
  val payload: ApprovalRequest?,
  override val revisionValue: RevisionValue
) : Revisionable