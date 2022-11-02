package io.acme.todo

import java.util.UUID

import io.lemonlabs.uri.Urn
import zio.*
import zio.json.*

import io.funkode.rest.ApiError

case class Todo(description: String, done: Boolean = false) derives JsonCodec

trait TodoService:
  def getTodos: IO[ApiError, List[Todo]]
  def createTodo(description: String): IO[ApiError, Todo]

object TodoService:

  import outbound.*

  def withTodoService[E, A](call: TodoService => ZIO[TodoService, E, A]) =
    ZIO.serviceWithZIO[TodoService](call)

  def getTodos: ZIO[TodoService, ApiError, List[Todo]] = withTodoService(_.getTodos)
  def createTodo(desc: String): ZIO[TodoService, ApiError, Todo] = withTodoService(_.createTodo(desc))

  class TodoServiceImpl(todosRepository: TodoRepository) extends TodoService:

    import todosRepository.*
    def getTodos: IO[ApiError, List[Todo]] =
      for todos <- getAllTodos()
      yield todos.filter(!_.done)

    def createTodo(description: String): IO[ApiError, Todo] =
      saveTodo(Urn("todo", UUID.randomUUID().toString), Todo(description))

  def live: ZLayer[TodoRepository, Nothing, TodoService] =
    ZLayer {
      for todoRep <- ZIO.service[TodoRepository]
      yield new TodoServiceImpl(todoRep)
    }
