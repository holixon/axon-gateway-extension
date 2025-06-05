package io.holixon.axon.gateway.example.approval.usecase

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holixon.axon.gateway.example.approval.read.ApprovalRequest
import io.holixon.axon.gateway.example.approval.read.ApprovalRequestQuery
import io.holixon.axon.gateway.example.approval.write.CreateApprovalRequestCommand
import io.holixon.axon.gateway.example.approval.write.UpdateApprovalRequestCommand
import io.holixon.axon.gateway.query.QueryResponseMessageResponseType
import io.holixon.axon.gateway.query.RevisionQueryParameters
import io.holixon.axon.gateway.query.RevisionValue
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.GenericMessage
import org.axonframework.queryhandling.QueryGateway
import org.springframework.stereotype.Service
import java.util.*

private val logger = KotlinLogging.logger {}

@Service
internal class TestScenario(
  val commandGateway: CommandGateway,
  val queryGateway: QueryGateway
) {
  fun createRequest(requestId: String = UUID.randomUUID().toString(), revision: Long = 1L): String {
    return commandGateway.send<String>(
      GenericCommandMessage.asCommandMessage<CreateApprovalRequestCommand>(
        CreateApprovalRequestCommand(
          requestId = requestId,
          subject = "Subject",
          currency = "EUR",
          amount = "42"
        )
      ).withMetaData(RevisionValue(revision).toMetaData().apply {
        logger.info { "Sending create command for $requestId with metadata $this" }
      })
    ).join().let {
      requestId
    }
  }

  fun updateRequest(requestId: String = UUID.randomUUID().toString(), revision: Long = 1L): String {
    return commandGateway.send<String>(
      GenericCommandMessage.asCommandMessage<UpdateApprovalRequestCommand>(
        UpdateApprovalRequestCommand(
          requestId = requestId,
          subject = "Subject",
          currency = "EUR",
          amount = "43"
        )
      ).withMetaData(RevisionValue(revision).toMetaData().apply {
        logger.info { "Sending update command for $requestId with metadata $this" }
      })
    ).join().let {
      requestId
    }
  }

  fun queryForRequest(requestId: String, revision: Long = 1L): ApprovalRequest? {
    return queryGateway
      .query(
        GenericMessage(ApprovalRequestQuery(requestId.trim()), RevisionQueryParameters(revision).toMetaData()),
        QueryResponseMessageResponseType.queryResponseMessageResponseType<ApprovalRequest>()
      ).handle { result, throwable ->
        if (throwable == null) {
          result
        } else {
          logger.error(throwable) { "Error occurred during query." }
          null
        }
      }
      .join()
  }
}