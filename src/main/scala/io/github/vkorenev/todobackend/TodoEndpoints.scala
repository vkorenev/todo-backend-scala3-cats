package io.github.vkorenev.todobackend

import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.bundle.SwaggerInterpreter

case class TodoEndpoints[F[_]]():
  private def healthEndpoint = endpoint.get
    .in("health")
    .out(stringBody)
    .summary("Health check")
    .description("Returns the health status of the service")
    .serverLogicSuccessPure[F](_ => "OK")

  private val serverEndpoints = List(
    healthEndpoint
  )

  private val swaggerEndpoints =
    SwaggerInterpreter().fromServerEndpoints[F](serverEndpoints, "Todo Backend API", "1.0.0")

  val allEndpoints: List[ServerEndpoint[Fs2Streams[F], F]] = serverEndpoints ++ swaggerEndpoints
