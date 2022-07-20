package io.holixon.axon.gateway.example

import io.holixon.axon.gateway.query.QueryResponseMessageResponseType
import io.holixon.axon.gateway.query.RevisionQueryParameters
import io.holixon.axon.gateway.query.RevisionValue
import mu.KLogging
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

/**
 * Command controller.
 */
@RestController
@RequestMapping("/approval-request")
class ApprovalRequestWriteController(
  private val commandGateway: CommandGateway
) {

  companion object : KLogging()

  private val counter = AtomicLong(1)

  /**
   * Creates a new approval request.
   * @param value approval request.
   */
  @PutMapping
  fun create(
    @RequestBody
    @Valid
    value: ApprovalRequestDto
  ): ResponseEntity<Void> {
    val requestId = UUID.randomUUID().toString()
    commandGateway.send<Void>(
      GenericCommandMessage.asCommandMessage<CreateApprovalRequestCommand>(
        CreateApprovalRequestCommand(
          requestId = requestId,
          subject = value.subject,
          currency = value.currency,
          amount = value.amount
        )
      ).withMetaData(RevisionValue(counter.getAndIncrement().also {
        logger.info("Sending create command for $requestId with revision $it")
      }).toMetaData())
    ).join()
    return created(
      ServletUriComponentsBuilder
        .fromCurrentServletMapping()
        .path("/approval-request/{id}")
        .buildAndExpand(requestId)
        .toUri()
    )
      .header("X-Revision", counter.get().toString())
      .build()
  }

  /**
   * Updates existing approval request.
   * @param requestId id of the request.
   * @param value new version of request.
   */
  @PostMapping("/{id}")
  fun update(
    @PathVariable("id")
    requestId: String,
    @RequestBody
    @Valid
    value: ApprovalRequestDto
  ): ResponseEntity<Void> {
    commandGateway.send<Void>(
      GenericCommandMessage.asCommandMessage<UpdateApprovalRequestCommand>(
        UpdateApprovalRequestCommand(
          requestId = requestId,
          subject = value.subject,
          currency = value.currency,
          amount = value.amount
        )
      ).withMetaData(RevisionValue(counter.getAndIncrement().also {
        logger.info("Sending update command for $requestId with revision $it")
      }).toMetaData())
    ).join()
    return noContent()
      .header("X-Revision", counter.get().toString())
      .build()
  }
}

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
        GenericCommandMessage
          .asCommandMessage<ApprovalRequestQuery>(ApprovalRequestQuery(requestId.trim()))
          .withMetaData(RevisionQueryParameters(revision).toMetaData()),
        QueryResponseMessageResponseType.queryResponseMessageResponseType<ApprovalRequest>()
      )
      .thenApply { result ->
        ok(
          ApprovalRequestDto(
            subject = result.subject,
            amount = result.amount,
            currency = result.currency
          )
        )
      }
      .exceptionally { notFound().build() }
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
        GenericCommandMessage
          .asCommandMessage<ApprovalRequestQuery>(ApprovalRequestQuery(requestId.trim()))
          .withMetaData(RevisionQueryParameters(revision).toMetaData()),
        ResponseTypes.instanceOf(ApprovalRequestQueryResult::class.java)
      )
      .thenApply { result ->
        if (result.payload != null) {
          ok(
            ApprovalRequestDto(
              subject = result.payload.subject,
              amount = result.payload.amount,
              currency = result.payload.currency
            )
          )
        } else {
          notFound().build()
        }
      }
      .exceptionally { notFound().build() }
      .join()
  }

}

/**
 * DTO.
 */
data class ApprovalRequestDto(
  @NotEmpty
  val subject: String,
  @NotEmpty
  val amount: String,
  @NotEmpty
  val currency: String = "EUR"
)