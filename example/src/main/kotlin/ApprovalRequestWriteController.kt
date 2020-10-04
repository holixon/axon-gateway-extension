package io.holixon.axon.gateway.example

import io.holixon.axon.gateway.query.RevisionQueryParameters
import io.holixon.axon.gateway.query.RevisionValue
import io.swagger.annotations.Api
import io.swagger.annotations.ApiModelProperty
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Api(tags = ["Command"])
@RestController
@RequestMapping("/approval-request")
class ApprovalRequestWriteController(
    private val commandGateway: CommandGateway
) {

  companion object {
    @JvmStatic
    val logger: Logger = LoggerFactory.getLogger(ApprovalRequestWriteController::class.java)
  }

  private val counter = AtomicLong(1)

  @ApiOperation(value = "Creates a new approval")
  @PutMapping
  fun create(@ApiParam("Approval request")
             @RequestBody
             @Valid
             value: ApprovalRequestDto): ResponseEntity<Void> {
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
    return created(ServletUriComponentsBuilder
        .fromCurrentServletMapping()
        .path("/{id}")
        .buildAndExpand(requestId)
        .toUri()).build()
  }

  @ApiOperation(
      value = "Updates exiting approval request."
  )
  @PostMapping("/{id}")
  fun update(
      @ApiParam("Id of approval request.")
      @PathVariable("id")
      requestId: String,
      @ApiParam("Approval request")
      @RequestBody
      @Valid
      value: ApprovalRequestDto): ResponseEntity<Void> {
    commandGateway.send<Void>(
        GenericCommandMessage.asCommandMessage<UpdateApprovalRequestCommand>(
            CreateApprovalRequestCommand(
                requestId = requestId,
                subject = value.subject,
                currency = value.currency,
                amount = value.amount
            )
        ).withMetaData(RevisionValue(counter.getAndIncrement().also {
          logger.info("Sending update command for $requestId with revision $it")
        }).toMetaData())
    ).join()
    return noContent().build()
  }
}

@Api(tags = ["Query"])
@RestController
@RequestMapping("/approval-request")
class ApprovalRequestReadController(
    private val queryGateway: QueryGateway
) {
  @ApiOperation(
      value = "Gets approval request."
  )
  @GetMapping("/{id}")
  fun getApprovalRequest(
      @PathVariable("id") requestId: String,
      @RequestParam("revision", defaultValue = "1") revision: Long = 1L): ResponseEntity<ApprovalRequestDto> {

    val result: ApprovalRequestQueryResult = queryGateway.query(
        GenericCommandMessage
            .asCommandMessage<ApprovalRequestQuery>(ApprovalRequestQuery(requestId.trim()))
            .withMetaData(RevisionQueryParameters(revision).toMetaData()),
        ResponseTypes.instanceOf(ApprovalRequestQueryResult::class.java)
    ).join()

    return ok(
        ApprovalRequestDto(
            subject = result.payload.subject,
            amount = result.payload.amount,
            currency = result.payload.currency
        )
    )
  }

}

/**
 * DTO.
 */
data class ApprovalRequestDto(
    @ApiModelProperty(name = "Subject", example = "Axon Advanced Training", dataType = "string", required = true)
    @NotEmpty
    val subject: String,
    @NotEmpty
    @ApiModelProperty(name = "Amount", example = "700.00", dataType = "string", required = true)
    val amount: String,
    @NotEmpty
    @ApiModelProperty(name = "Currency", example = "EUR", dataType = "string", required = true)
    val currency: String = "EUR"
)