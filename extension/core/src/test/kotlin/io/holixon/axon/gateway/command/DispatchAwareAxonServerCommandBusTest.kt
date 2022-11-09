package io.holixon.axon.gateway.command

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.security.AnyTypePermission
import io.axoniq.axonserver.connector.AxonServerConnection
import io.axoniq.axonserver.connector.command.CommandChannel
import io.axoniq.axonserver.grpc.command.CommandResponse
import io.mockk.called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.axonframework.axonserver.connector.AxonServerConfiguration
import org.axonframework.axonserver.connector.AxonServerConnectionManager
import org.axonframework.axonserver.connector.command.AxonServerCommandBus.builder
import org.axonframework.commandhandling.CommandBus
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.commandhandling.distributed.RoutingStrategy
import org.axonframework.messaging.MessageHandler
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.xml.XStreamSerializer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture

internal class DispatchAwareAxonServerCommandBusTest {

  companion object {
    const val COMMAND_NAME = "command"
  }

  private val configuration: AxonServerConfiguration = AxonServerConfiguration.builder().build()
  private val commandDispatchStrategy: CommandDispatchStrategy = mockk()
  private val messageHandler: MessageHandler<in CommandMessage<*>?> = mockk()
  private val localSegment: CommandBus = mockk()
  private val manager: AxonServerConnectionManager = mockk()
  private val connection: AxonServerConnection = mockk()
  private val axonServerCommandChannel: CommandChannel = mockk()
  private val routingStrategy: RoutingStrategy = mockk()
  private val serializer: Serializer = XStreamSerializer.builder().xStream(XStream().also { it.addPermission(AnyTypePermission.ANY) }).build()
  private lateinit var bus: DispatchAwareAxonServerCommandBus

  @BeforeEach
  fun `mock axon server connection manager`() {
    every { manager.getConnection(any()) }.returns(connection)
    every { connection.commandChannel() }.returns(axonServerCommandChannel)
    every { localSegment.subscribe(any(), any()) }.returns(mockk())
    every { localSegment.dispatch<Any, Any>(any(), any()) }.returns(mockk())
    every { axonServerCommandChannel.registerCommandHandler(any(), any(), any()) }.returns(mockk())
    every { axonServerCommandChannel.sendCommand(any()) }.returns(CompletableFuture.completedFuture(CommandResponse.getDefaultInstance()))
    every { routingStrategy.getRoutingKey(any()) }.returns("")


    bus = DispatchAwareAxonServerCommandBus(
      builder()
        .axonServerConnectionManager(manager)
        .localSegment(localSegment)
        .configuration(configuration)
        .serializer(serializer)
        .routingStrategy(routingStrategy),
      commandDispatchStrategy
    )
  }

  @Test
  fun `registers handler local only`() {
    every { commandDispatchStrategy.registerRemote(any(), any()) } returns false
    bus.subscribe(COMMAND_NAME, messageHandler)
    verify {
      localSegment.subscribe(COMMAND_NAME, messageHandler)
      axonServerCommandChannel wasNot called
    }
  }

  @Test
  fun `registers handler local and remote`() {
    every { commandDispatchStrategy.registerRemote(any(), any()) } returns true
    bus.subscribe(COMMAND_NAME, messageHandler)
    verify {
      localSegment.subscribe(COMMAND_NAME, messageHandler)
      axonServerCommandChannel.registerCommandHandler(any(), any(), COMMAND_NAME)
    }
  }

  @Test
  fun `dispatches command local only`() {

    every { commandDispatchStrategy.dispatchRemote<TestCommand, Void>(any(), any()) } returns false
    val message = GenericCommandMessage.asCommandMessage<TestCommand>(TestCommand("4711"))

    bus.dispatch(message)

    verify {
      localSegment.dispatch<TestCommand, Void>(message, any())
      axonServerCommandChannel wasNot called
    }
  }

  @Test
  fun `dispatches command remote`() {

    every { commandDispatchStrategy.dispatchRemote<TestCommand, Void>(any(), any()) } returns true
    val command = TestCommand("4711")
    val message = GenericCommandMessage.asCommandMessage<TestCommand>(command)

    bus.dispatch(message)

    verify {
      localSegment wasNot called
      axonServerCommandChannel.sendCommand(any())
    }
  }

  data class TestCommand(
    val id: String
  )
}