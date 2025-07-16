package io.github.vkorenev.todobackend

import cats.effect.IO
import cats.effect.IOApp
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.trace.TracerProvider

object Main extends IOApp.Simple:
  def run: IO[Unit] =
    (for {
      otel4s <- OtelJava.autoConfigured[IO]()
      _ <- registerRuntimeMetrics[IO](otel4s)
      todoService <- TodoService.make[IO]
      todoEndpoints = TodoEndpoints[IO](todoService)
      server <- {
        given MeterProvider[IO] = otel4s.meterProvider
        given TracerProvider[IO] = otel4s.tracerProvider
        TodoRoutes.server(todoEndpoints.routes)
      }
    } yield server).use(_ => IO.never)
