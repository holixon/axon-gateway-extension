package io.holixon.axon.gateway.example

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertNotNull

@SpringBootTest
@ActiveProfiles("jackson")
@DirtiesContext
@Disabled // FIXME: #115
class JacksonApplicationITest {

  @Autowired
  private lateinit var scenario: TestScenario

  @Test
  fun runScenario() {
    val requestId = scenario.createRequest()
    assertNotNull(scenario.queryForRequest(requestId), "Could not find request for id $requestId")
  }
}