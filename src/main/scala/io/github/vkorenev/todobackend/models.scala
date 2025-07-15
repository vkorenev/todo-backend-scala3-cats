package io.github.vkorenev.todobackend

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import sttp.tapir.Schema

case class TodoItem(
    id: Long,
    title: String,
    completed: Boolean,
    order: Option[Int]
)

case class Todo(
    title: String,
    completed: Boolean,
    url: String,
    order: Option[Int]
) derives Schema

case class CreateTodoRequest(
    title: String,
    order: Option[Int]
) derives Schema

case class UpdateTodoRequest(
    title: Option[String] = None,
    completed: Option[Boolean] = None,
    order: Option[Int] = None
) derives Schema

// JsonValueCodec instances for jsoniter-scala
given JsonValueCodec[Todo] = JsonCodecMaker.make
given JsonValueCodec[CreateTodoRequest] = JsonCodecMaker.make
given JsonValueCodec[UpdateTodoRequest] = JsonCodecMaker.make
given JsonValueCodec[List[Todo]] = JsonCodecMaker.make
