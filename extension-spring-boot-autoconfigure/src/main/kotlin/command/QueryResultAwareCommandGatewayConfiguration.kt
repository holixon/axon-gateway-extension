package io.holixon.axon.gateway.configuration.command

import io.holixon.axon.gateway.command.QueryResultAwareCommandGateway
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.queryhandling.QueryGateway
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class QueryResultAwareCommandGatewayConfiguration {

  @Bean
  fun queryResultAwareCommandGateway(commandGateway: CommandGateway, queryGateway: QueryGateway) = QueryResultAwareCommandGateway(
      commandGateway = commandGateway,
      queryGateway = queryGateway,
      defaultTimeout = 20
  )
}