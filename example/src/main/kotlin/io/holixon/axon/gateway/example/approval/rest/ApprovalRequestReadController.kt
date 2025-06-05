package io.holixon.axon.gateway.example.approval.rest

import io.holixon.axon.gateway.example.approval.read.ApprovalRequest
import io.holixon.axon.gateway.example.approval.read.ApprovalRequestQuery
import io.holixon.axon.gateway.example.approval.read.ApprovalRequestQueryResult
import io.holixon.axon.gateway.query.QueryResponseMessageResponseType
import io.holixon.axon.gateway.query.RevisionQueryParameters
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Query side controller.
 */
@RestController
@RequestMapping("/approval-request")
class ApprovalRequestReadController(
  private val queryGateway: QueryGateway
) {
  /**
   * Retrieves approval request by id.
   * @param requestId id of approval request.
   * @param revision minimal revision.
   */
  @GetMapping("/{id}")
  fun getApprovalRequest(
      @PathVariable("id") requestId: String,
      @RequestParam("revision", defaultValue = "1") revision: Long = 1L
  ): ResponseEntity<ApprovalRequestDto> {

    return queryGateway
      .query(
        GenericCommandMessage.asCommandMessage<ApprovalRequestQuery>(ApprovalRequestQuery(requestId.trim()))
          .withMetaData(RevisionQueryParameters(revision).toMetaData()),
          QueryResponseMessageResponseType.queryResponseMessageResponseType<ApprovalRequest>()
      )
      .thenApply { result ->
          ResponseEntity.ok(
              ApprovalRequestDto(
                  subject = result.subject,
                  amount = result.amount,
                  currency = result.currency
              )
          )
      }
      .exceptionally { ResponseEntity.notFound().build() }
      .join()
  }

  /**
   * Retrieves approval request by id.
   * @param requestId id of approval request.
   * @param revision minimal revision.
   */
  @GetMapping("/embedded/{id}")
  fun getApprovalRequestEmbedded(
      @PathVariable("id") requestId: String,
      @RequestParam("revision", defaultValue = "1") revision: Long = 1L
  ): ResponseEntity<ApprovalRequestDto> {

    return queryGateway
      .query(
        GenericCommandMessage.asCommandMessage<ApprovalRequestQuery>(ApprovalRequestQuery(requestId.trim()))
          .withMetaData(RevisionQueryParameters(revision).toMetaData()),
          ResponseTypes.instanceOf(ApprovalRequestQueryResult::class.java)
      )
      .thenApply { result ->
        if (result.payload != null) {
            ResponseEntity.ok(
                ApprovalRequestDto(
                    subject = result.payload.subject,
                    amount = result.payload.amount,
                    currency = result.payload.currency
                )
            )
        } else {
          ResponseEntity.notFound().build()
        }
      }
      .exceptionally { ResponseEntity.notFound().build() }
      .join()
  }

}