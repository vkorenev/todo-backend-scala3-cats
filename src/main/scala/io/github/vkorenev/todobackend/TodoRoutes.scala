package io.github.vkorenev.todobackend

import cats.effect.Async
import cats.effect.Resource
import com.comcast.ip4s.*
import fs2.io.net.Network
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.Server
import sttp.tapir.server.http4s.Http4sServerInterpreter

object TodoRoutes:

  def server[F[_]: {Async, Network}](todoEndpoints: TodoEndpoints[F]): Resource[F, Server] =
    val routes = Http4sServerInterpreter[F]().toRoutes(todoEndpoints.allEndpoints)
    val httpApp = Router("/" -> routes).orNotFound
    EmberServerBuilder
      .default[F]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(httpApp)
      .build
