package io.holixon.axon.gateway.example

import org.awaitility.Awaitility
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.lang.Thread.sleep
import java.time.Duration
import java.util.*
import kotlin.test.assertNotNull

@SpringBootTest
@DirtiesContext
@ActiveProfiles("inmem")
internal class XstreamApplicationITest {

  @Autowired
  private lateinit var scenario: TestScenario

  val requestId = UUID.randomUUID().toString()

  @Test
  fun `should create and query for request using XStream`() {
    // create request
    Thread {
      sleep(2_000)
      scenario.createRequest(requestId)
    }.start()

    Awaitility.await("Could not find request for id $requestId").atMost(Duration.ofSeconds(13)).untilAsserted {
      assertNotNull(scenario.queryForRequest(requestId))
    }
  }
}