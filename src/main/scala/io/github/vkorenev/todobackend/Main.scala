package io.github.vkorenev.todobackend

import cats.effect.IO
import cats.effect.IOApp

object Main extends IOApp.Simple:
  def run: IO[Unit] = TodoRoutes.server(TodoEndpoints[IO]()).use(_ => IO.never)
