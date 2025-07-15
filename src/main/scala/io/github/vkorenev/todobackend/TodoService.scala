package io.github.vkorenev.todobackend

import cats.effect.Async
import cats.effect.Resource
import cats.syntax.all.*
import doobie.*
import doobie.hikari.HikariTransactor
import doobie.implicits.*

trait TodoService[F[_]]:
  def getAllTodos: F[List[TodoItem]]
  def getTodoById(id: Long): F[Option[TodoItem]]
  def createTodo(request: CreateTodoRequest): F[TodoItem]
  def updateTodo(id: Long, request: UpdateTodoRequest): F[Option[TodoItem]]
  def deleteTodo(id: Long): F[Boolean]
  def deleteAllTodos: F[Unit]

object TodoService:
  def make[F[_]: Async]: Resource[F, TodoService[F]] =
    for
      xa <- HikariTransactor.newHikariTransactor[F](
        driverClassName = "org.h2.Driver",
        url = s"jdbc:h2:mem:todoapp_${System.nanoTime()};DB_CLOSE_DELAY=-1",
        user = "sa",
        pass = "",
        connectEC = scala.concurrent.ExecutionContext.global,
        logHandler = None
      )
      _ <- Resource.eval(createSchema(xa))
    yield new DatabaseTodoService(xa)

  private def createSchema[F[_]: Async](xa: Transactor[F]): F[Unit] =
    sql"""
      CREATE TABLE IF NOT EXISTS todos (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        title VARCHAR(255) NOT NULL,
        completed BOOLEAN NOT NULL DEFAULT FALSE,
        order_num INTEGER
      )
    """.update.run.transact(xa).void

class DatabaseTodoService[F[_]: Async](xa: Transactor[F]) extends TodoService[F]:

  def getAllTodos: F[List[TodoItem]] =
    sql"SELECT id, title, completed, order_num FROM todos"
      .query[TodoItem]
      .to[List]
      .transact(xa)

  def getTodoById(id: Long): F[Option[TodoItem]] =
    sql"SELECT id, title, completed, order_num FROM todos WHERE id = $id"
      .query[TodoItem]
      .option
      .transact(xa)

  def createTodo(request: CreateTodoRequest): F[TodoItem] =
    sql"INSERT INTO todos (title, completed, order_num) VALUES (${request.title}, false, ${request.order})".update
      .withUniqueGeneratedKeys[Long]("id")
      .transact(xa)
      .flatMap { generatedId =>
        getTodoById(generatedId).map(_.get) // We know it exists since we just created it
      }

  def updateTodo(id: Long, request: UpdateTodoRequest): F[Option[TodoItem]] =
    for
      existingTodo <- getTodoById(id)
      result <- existingTodo match
        case Some(existing) =>
          val updatedTodo = existing.copy(
            title = request.title.getOrElse(existing.title),
            completed = request.completed.getOrElse(existing.completed),
            order = request.order.orElse(existing.order)
          )
          sql"UPDATE todos SET title = ${updatedTodo.title}, completed = ${updatedTodo.completed}, order_num = ${updatedTodo.order} WHERE id = $id".update.run
            .transact(xa)
            .as(Some(updatedTodo))
        case None =>
          Async[F].pure(None)
    yield result

  def deleteTodo(id: Long): F[Boolean] =
    sql"DELETE FROM todos WHERE id = $id".update.run
      .transact(xa)
      .map(_ > 0)

  def deleteAllTodos: F[Unit] =
    sql"DELETE FROM todos".update.run
      .transact(xa)
      .void
