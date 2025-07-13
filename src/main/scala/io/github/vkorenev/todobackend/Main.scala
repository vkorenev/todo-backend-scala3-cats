package io.github.vkorenev.todobackend

import cats.effect.IO
import cats.effect.IOApp
import org.typelevel.otel4s.oteljava.OtelJava

object Main extends IOApp.Simple:
  def run: IO[Unit] =
    (for {
      otel4s <- OtelJava.autoConfigured[IO]()
      _ <- registerRuntimeMetrics[IO](otel4s)
      server <- TodoRoutes.server(TodoEndpoints[IO]())
    } yield server).use(_ => IO.never)
