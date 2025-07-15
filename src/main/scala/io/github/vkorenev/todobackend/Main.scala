package io.github.vkorenev.todobackend

import cats.effect.IO
import cats.effect.IOApp
import org.typelevel.otel4s.oteljava.OtelJava

object Main extends IOApp.Simple:
  def run: IO[Unit] =
    (for {
      otel4s <- OtelJava.autoConfigured[IO]()
      _ <- registerRuntimeMetrics[IO](otel4s)
      todoService <- TodoService.make[IO]
      todoEndpoints = TodoEndpoints[IO](todoService)
      server <- TodoRoutes.server(todoEndpoints)
    } yield server).use(_ => IO.never)
