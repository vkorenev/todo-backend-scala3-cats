package io.github.vkorenev.todobackend

import cats.effect.Async
import cats.syntax.all.*
import org.http4s.HttpRoutes
import sttp.tapir.*
import sttp.tapir.json.jsoniter.*
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.server.http4s.Http4sServerOptions
import sttp.tapir.server.interceptor.cors.CORSInterceptor
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import java.util.UUID

case class TodoEndpoints[F[_]: Async](todoService: TodoService[F]):
  private val baseUri = "https://todo-backend-wggmkml2fa-uw.a.run.app"

  case class TodoNotFound(id: UUID) extends Exception(s"Todo with id $id not found")

  private def toApiTodo(todoItem: TodoItem): Todo =
    Todo(
      title = todoItem.title,
      completed = todoItem.completed,
      url = s"$baseUri/todos/${todoItem.id}"
    )

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
    .serverLogicSuccess[F] { _ =>
      todoService.getAllTodos.map(_.map(toApiTodo))
    }

  private def createTodoEndpoint = endpoint.post
    .in("todos")
    .in(jsonBody[CreateTodoRequest])
    .out(jsonBody[Todo])
    .summary("Create a new todo")
    .description("Creates a new todo item")
    .serverLogicSuccess[F] { request =>
      todoService.createTodo(request).map(toApiTodo)
    }

  private def parseUUID(str: String): F[UUID] =
    Async[F].catchNonFatal(UUID.fromString(str))

  private def getTodoEndpoint = endpoint.get
    .in("todos" / path[String]("id"))
    .out(jsonBody[Todo])
    .summary("Get a todo by ID")
    .description("Returns a specific todo item by its ID")
    .serverLogicSuccess[F] { id =>
      parseUUID(id).flatMap { uuid =>
        todoService.getTodoById(uuid).flatMap {
          case Some(todoItem) => Async[F].pure(toApiTodo(todoItem))
          case None => Async[F].raiseError(TodoNotFound(uuid))
        }
      }
    }

  private def updateTodoEndpoint = endpoint.patch
    .in("todos" / path[String]("id"))
    .in(jsonBody[UpdateTodoRequest])
    .out(jsonBody[Todo])
    .summary("Update a todo by ID")
    .description("Updates a specific todo item by its ID")
    .serverLogicSuccess[F] { case (id, updateRequest) =>
      parseUUID(id).flatMap { uuid =>
        todoService.updateTodo(uuid, updateRequest).flatMap {
          case Some(todoItem) => Async[F].pure(toApiTodo(todoItem))
          case None => Async[F].raiseError(TodoNotFound(uuid))
        }
      }
    }

  private def deleteTodoEndpoint = endpoint.delete
    .in("todos" / path[String]("id"))
    .out(stringBody)
    .summary("Delete a todo by ID")
    .description("Deletes a specific todo item by its ID")
    .serverLogicSuccess[F] { id =>
      parseUUID(id).flatMap { uuid =>
        todoService.deleteTodo(uuid).flatMap { deleted =>
          if deleted then Async[F].pure(s"Todo $id deleted")
          else Async[F].raiseError(TodoNotFound(uuid))
        }
      }
    }

  private def deleteAllTodosEndpoint = endpoint.delete
    .in("todos")
    .out(stringBody)
    .summary("Delete all todos")
    .description("Deletes all todo items")
    .serverLogicSuccess[F] { _ =>
      todoService.deleteAllTodos.as("All todos deleted")
    }

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
