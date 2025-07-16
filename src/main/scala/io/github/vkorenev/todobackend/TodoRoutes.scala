package io.github.vkorenev.todobackend

import cats.effect.Async
import cats.effect.Resource
import com.comcast.ip4s.*
import fs2.io.net.Network
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.otel4s.middleware.metrics.OtelMetrics
import org.http4s.otel4s.middleware.trace.redact.PathRedactor
import org.http4s.otel4s.middleware.trace.redact.QueryRedactor
import org.http4s.otel4s.middleware.trace.server.ServerMiddleware
import org.http4s.otel4s.middleware.trace.server.ServerSpanDataProvider
import org.http4s.server.Server
import org.http4s.server.middleware.Metrics
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.trace.TracerProvider

object TodoRoutes:
  private val pathAndQueryRedactor = new PathRedactor.NeverRedact with QueryRedactor.NeverRedact {}

  def server[F[_]: {Async, Network, MeterProvider, TracerProvider}](
      todoEndpoints: TodoEndpoints[F]
  ): Resource[F, Server] =
    for {
      metricsOps <- Resource.eval(OtelMetrics.serverMetricsOps[F]())
      serverMiddleware <- Resource.eval(
        ServerMiddleware
          .builder[F](ServerSpanDataProvider.openTelemetry(pathAndQueryRedactor))
          .build
      )
      server <- EmberServerBuilder
        .default[F]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(
          serverMiddleware
            .wrapHttpRoutes(Metrics(metricsOps)(todoEndpoints.routes))
            .orNotFound
        )
        .build
    } yield server
