package io.github.vkorenev.todobackend

import cats.effect.Ref
import cats.effect.Sync
import cats.syntax.all.*

import java.util.UUID

trait TodoService[F[_]]:
  def getAllTodos: F[List[TodoItem]]
  def getTodoById(id: UUID): F[Option[TodoItem]]
  def createTodo(request: CreateTodoRequest): F[TodoItem]
  def updateTodo(id: UUID, request: UpdateTodoRequest): F[Option[TodoItem]]
  def deleteTodo(id: UUID): F[Boolean]
  def deleteAllTodos: F[Unit]

object TodoService:
  def make[F[_]: Sync]: F[TodoService[F]] =
    Ref.of[F, Map[UUID, TodoItem]](Map.empty).map(new InMemoryTodoService(_))

class InMemoryTodoService[F[_]: Sync](todosRef: Ref[F, Map[UUID, TodoItem]]) extends TodoService[F]:

  def getAllTodos: F[List[TodoItem]] =
    todosRef.get.map(_.values.toList)

  def getTodoById(id: UUID): F[Option[TodoItem]] =
    todosRef.get.map(_.get(id))

  def createTodo(request: CreateTodoRequest): F[TodoItem] =
    for
      id <- Sync[F].delay(UUID.randomUUID())
      todo = TodoItem(id = id, title = request.title, completed = false)
      _ <- todosRef.update(_ + (id -> todo))
    yield todo

  def updateTodo(id: UUID, request: UpdateTodoRequest): F[Option[TodoItem]] =
    todosRef.modify { todos =>
      todos.get(id) match
        case Some(existingTodo) =>
          val updatedTodo = existingTodo.copy(
            title = request.title.getOrElse(existingTodo.title),
            completed = request.completed.getOrElse(existingTodo.completed)
          )
          val updatedTodos = todos + (id -> updatedTodo)
          (updatedTodos, Some(updatedTodo))
        case None =>
          (todos, None)
    }

  def deleteTodo(id: UUID): F[Boolean] =
    todosRef.modify { todos =>
      if todos.contains(id) then (todos - id, true)
      else (todos, false)
    }

  def deleteAllTodos: F[Unit] =
    todosRef.set(Map.empty)
