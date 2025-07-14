package io.github.vkorenev.todobackend

import cats.effect.Async
import org.http4s.HttpRoutes
import sttp.tapir.*
import sttp.tapir.json.jsoniter.*
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.server.http4s.Http4sServerOptions
import sttp.tapir.server.interceptor.cors.CORSInterceptor
import sttp.tapir.swagger.bundle.SwaggerInterpreter

case class TodoEndpoints[F[_]: Async]():
  private val baseUri = "https://todo-backend-wggmkml2fa-uw.a.run.app"

  private def healthEndpoint = endpoint.get
    .in("health")
    .out(stringBody)
    .summary("Health check")
    .description("Returns the health status of the service")
    .serverLogicSuccessPure[F](_ => "OK")

  private def getAllTodosEndpoint = endpoint.get
    .in("todos")
    .out(jsonBody[List[Todo]])
    .summary("Get all todos")
    .description("Returns a list of all todos")
    .serverLogicSuccessPure[F](_ => List.empty[Todo])

  private def createTodoEndpoint = endpoint.post
    .in("todos")
    .in(jsonBody[CreateTodoRequest])
    .out(jsonBody[Todo])
    .summary("Create a new todo")
    .description("Creates a new todo item")
    .serverLogicSuccessPure[F] { request =>
      Todo(
        title = request.title,
        completed = false,
        url = s"$baseUri/todos/1"
      )
    }

  private def getTodoEndpoint = endpoint.get
    .in("todos" / path[String]("id"))
    .out(jsonBody[Todo])
    .summary("Get a todo by ID")
    .description("Returns a specific todo item by its ID")
    .serverLogicSuccessPure[F] { id =>
      Todo(
        title = s"Todo $id",
        completed = false,
        url = s"$baseUri/todos/$id"
      )
    }

  private def updateTodoEndpoint = endpoint.patch
    .in("todos" / path[String]("id"))
    .in(jsonBody[UpdateTodoRequest])
    .out(jsonBody[Todo])
    .summary("Update a todo by ID")
    .description("Updates a specific todo item by its ID")
    .serverLogicSuccessPure[F] { case (id, updateRequest) =>
      Todo(
        title = updateRequest.title.getOrElse(s"Todo $id"),
        completed = updateRequest.completed.getOrElse(false),
        url = s"$baseUri/todos/$id"
      )
    }

  private def deleteTodoEndpoint = endpoint.delete
    .in("todos" / path[String]("id"))
    .out(stringBody)
    .summary("Delete a todo by ID")
    .description("Deletes a specific todo item by its ID")
    .serverLogicSuccessPure[F] { id =>
      s"Todo $id deleted"
    }

  private def deleteAllTodosEndpoint = endpoint.delete
    .in("todos")
    .out(stringBody)
    .summary("Delete all todos")
    .description("Deletes all todo items")
    .serverLogicSuccessPure[F](_ => "All todos deleted")

  def routes: HttpRoutes[F] =
    val serverEndpoints = List(
      healthEndpoint,
      getAllTodosEndpoint,
      createTodoEndpoint,
      getTodoEndpoint,
      updateTodoEndpoint,
      deleteTodoEndpoint,
      deleteAllTodosEndpoint
    )

    val swaggerEndpoints =
      SwaggerInterpreter().fromServerEndpoints[F](serverEndpoints, "Todo Backend API", "1.0.0")

    Http4sServerInterpreter[F](
      Http4sServerOptions.customiseInterceptors.corsInterceptor(CORSInterceptor.default).options
    ).toRoutes(serverEndpoints ++ swaggerEndpoints)
