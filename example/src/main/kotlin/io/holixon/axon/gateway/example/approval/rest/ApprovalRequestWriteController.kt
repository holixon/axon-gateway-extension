package io.holixon.axon.gateway.example.approval.rest

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holixon.axon.gateway.example.approval.write.CreateApprovalRequestCommand
import io.holixon.axon.gateway.example.approval.write.UpdateApprovalRequestCommand
import io.holixon.axon.gateway.query.RevisionValue
import jakarta.validation.Valid
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.*
import java.util.concurrent.atomic.AtomicLong

private val logger = KotlinLogging.logger {}
/**
 * Command controller.
 */
@RestController
@RequestMapping("/approval-request")
class ApprovalRequestWriteController(
  private val commandGateway: CommandGateway
) {


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
          logger.info{ "Sending create command for $requestId with revision $it" }
      }).toMetaData())
    ).join()
    return ResponseEntity.created(
        ServletUriComponentsBuilder.fromCurrentServletMapping()
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
          logger.info { "Sending update command for $requestId with revision $it" }
      }).toMetaData())
    ).join()
    return ResponseEntity.noContent()
      .header("X-Revision", counter.get().toString())
      .build()
  }
}