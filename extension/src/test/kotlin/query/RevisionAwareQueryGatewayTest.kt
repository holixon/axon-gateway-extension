package query

import io.holixon.axon.gateway.query.QueryResponseMessageResponseType.Companion.queryResponseMessageResponseType
import io.holixon.axon.gateway.query.RevisionAwareQueryGateway
import io.holixon.axon.gateway.query.RevisionQueryParameters
import io.holixon.axon.gateway.query.RevisionValue
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.axonframework.common.Registration
import org.axonframework.messaging.GenericMessage
import org.axonframework.queryhandling.*
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.publisher.TestPublisher
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class RevisionAwareQueryGatewayTest {
  private val queryBus = mockk<QueryBus>()

  private val queryGateway = RevisionAwareQueryGateway(queryBus, defaultTimeout = 5)

  private object TestQuery

  @Test
  fun `cancels subscription query after result is received`() {
    val subscriptionQueryUpdatePublisher = TestPublisher.create<SubscriptionQueryUpdateMessage<String>>()
    val registration = mockk<Registration>()
    every {
      queryBus.subscriptionQuery(
        any<SubscriptionQueryMessage<TestQuery, String, String>>(),
        any(),
        any()
      )
    } returns DefaultSubscriptionQueryResult<QueryResponseMessage<String>, SubscriptionQueryUpdateMessage<String>>(
      Mono.just(GenericQueryResponseMessage("foo1").withMetaData(RevisionQueryParameters(1).toMetaData())),
      subscriptionQueryUpdatePublisher.flux(),
      registration
    )
    every { registration.cancel() } returns true

    val result = queryGateway.query(GenericMessage.asMessage(TestQuery).withMetaData(RevisionQueryParameters(4).toMetaData()), queryResponseMessageResponseType<String>())

    subscriptionQueryUpdatePublisher.next(updateMessage("foo2", 2))
    assertFalse(result.isDone, "Result future should not be done yet.")
    subscriptionQueryUpdatePublisher.next(updateMessage("foo3", 3))
    assertFalse(result.isDone, "Result future should not be done yet.")
    subscriptionQueryUpdatePublisher.next(updateMessage("foo4", 4))
    assertTrue(result.isDone, "Result future should be done now.")
    assertEquals("foo4", result.join())
    subscriptionQueryUpdatePublisher.assertCancelled()
    verify { registration.cancel() }
  }

  private fun updateMessage(payload: String, revision: Long) = GenericSubscriptionQueryUpdateMessage(payload).withMetaData(RevisionValue(revision).toMetaData())
}