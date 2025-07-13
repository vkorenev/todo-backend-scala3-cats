package io.github.vkorenev.todobackend

import cats.effect.Resource
import cats.effect.Sync
import cats.syntax.functor.*
import io.opentelemetry.instrumentation.runtimemetrics.java17.RuntimeMetrics
import org.typelevel.otel4s.oteljava.OtelJava

def registerRuntimeMetrics[F[_]: Sync](otel4s: OtelJava[F]): Resource[F, Unit] =
  Resource
    .fromAutoCloseable(Sync[F].delay {
      RuntimeMetrics
        .builder(otel4s.underlying)
        .enableAllFeatures()
        .captureGcCause()
        .emitExperimentalTelemetry()
        .build()
    })
    .void
