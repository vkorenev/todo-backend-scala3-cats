package io.github.vkorenev.todobackend

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import sttp.tapir.Schema

case class Todo(
    title: String,
    completed: Boolean,
    url: String
) derives Schema

case class CreateTodoRequest(
    title: String
) derives Schema

case class UpdateTodoRequest(
    title: Option[String] = None,
    completed: Option[Boolean] = None
) derives Schema

// JsonValueCodec instances for jsoniter-scala
given JsonValueCodec[Todo] = JsonCodecMaker.make
given JsonValueCodec[CreateTodoRequest] = JsonCodecMaker.make
given JsonValueCodec[UpdateTodoRequest] = JsonCodecMaker.make
given JsonValueCodec[List[Todo]] = JsonCodecMaker.make
