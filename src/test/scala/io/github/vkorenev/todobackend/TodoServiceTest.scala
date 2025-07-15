package io.github.vkorenev.todobackend

import cats.effect.IO
import munit.CatsEffectSuite

import java.util.UUID

class TodoServiceTest extends CatsEffectSuite:

  test("create and retrieve todo") {
    for
      service <- TodoService.make[IO]
      request = CreateTodoRequest("Test todo", None)
      created <- service.createTodo(request)
      retrieved <- service.getTodoById(created.id)
    yield {
      assertEquals(created.title, "Test todo")
      assertEquals(created.completed, false)
      assertEquals(retrieved, Some(created))
    }
  }

  test("update todo") {
    for
      service <- TodoService.make[IO]
      request = CreateTodoRequest("Test todo", None)
      created <- service.createTodo(request)
      updateRequest = UpdateTodoRequest(Some("Updated todo"), Some(true), None)
      updated <- service.updateTodo(created.id, updateRequest)
    yield {
      assert(updated.isDefined)
      assertEquals(updated.get.title, "Updated todo")
      assertEquals(updated.get.completed, true)
      assertEquals(updated.get.id, created.id)
    }
  }

  test("delete todo") {
    for
      service <- TodoService.make[IO]
      request = CreateTodoRequest("Test todo", None)
      created <- service.createTodo(request)
      deleted <- service.deleteTodo(created.id)
      retrieved <- service.getTodoById(created.id)
    yield {
      assertEquals(deleted, true)
      assertEquals(retrieved, None)
    }
  }

  test("get all todos") {
    for
      service <- TodoService.make[IO]
      request1 = CreateTodoRequest("Todo 1", None)
      request2 = CreateTodoRequest("Todo 2", None)
      _ <- service.createTodo(request1)
      _ <- service.createTodo(request2)
      todos <- service.getAllTodos
    yield {
      assertEquals(todos.length, 2)
      assert(todos.exists(_.title == "Todo 1"))
      assert(todos.exists(_.title == "Todo 2"))
    }
  }
