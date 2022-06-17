package io.holixon.axon.gateway.example

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import kotlin.test.assertNotNull

@SpringBootTest
@DirtiesContext
internal class XstreamApplicationITest {

  @Autowired
  private lateinit var scenario: TestScenario

  @Test
  fun runScenario() {
    val requestId = scenario.createRequest()
    assertNotNull(scenario.queryForRequest(requestId), "Could not find request for id $requestId")
  }
}