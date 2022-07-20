package io.holixon.axon.gateway.example

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.util.*
import kotlin.test.assertNotNull

@SpringBootTest
@ActiveProfiles("jackson", "inmem")
@DirtiesContext
internal class JacksonApplicationITest {

  @Autowired
  private lateinit var scenario: TestScenario

  val requestId = UUID.randomUUID().toString()

  @Test
  fun `should create and query for request using jackson`() {
    scenario.createRequest(requestId, revision = 1L)

    assertNotNull(scenario.queryForRequest(requestId, revision = 1L))

    Thread {
      Thread.sleep(500)
      scenario.updateRequest(requestId, revision = 2L)
    }.start()

    assertNotNull(scenario.queryForRequest(requestId, revision = 2L))
  }
}