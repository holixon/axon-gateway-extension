package io.holixon.axon.gateway.example

import io.holixon.axon.gateway.query.QueryResponseMessageResponseType
import io.holixon.axon.gateway.query.RevisionQueryParameters
import io.holixon.axon.gateway.query.RevisionValue
import mu.KLogging
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.queryhandling.QueryGateway
import org.springframework.stereotype.Service
import java.util.*


@Service
internal class TestScenario(
  val commandGateway: CommandGateway,
  val queryGateway: QueryGateway
) {
  companion object : KLogging() {
    const val revision = 1L
  }

  fun createRequest(requestId: String = UUID.randomUUID().toString()): String {
    return commandGateway.send<String>(
      GenericCommandMessage.asCommandMessage<CreateApprovalRequestCommand>(
        CreateApprovalRequestCommand(
          requestId = requestId,
          subject = "Subject",
          currency = "EUR",
          amount = "42"
        )
      ).withMetaData(RevisionValue(revision).toMetaData().apply {
        logger.info("Sending create command for $requestId with metadata $this")
      })
    ).join().let {
      requestId
    }
  }

  fun queryForRequest(requestId: String): ApprovalRequest? {
    return queryGateway
      .query(
        GenericCommandMessage
          .asCommandMessage<ApprovalRequestQuery>(ApprovalRequestQuery(requestId.trim()))
          .withMetaData(RevisionQueryParameters(revision).toMetaData()),
        QueryResponseMessageResponseType.queryResponseMessageResponseType<ApprovalRequest>()
      ).handle { result, throwable ->
        if (throwable == null) {
          result
        } else {
          logger.error(throwable) { "Error occured during query." }
          null
        }
      }
      .join()
  }
}