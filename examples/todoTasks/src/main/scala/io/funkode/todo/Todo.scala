package io.funkode.todo

import zio.*
import zio.json.*

case class Todo(id: Int, description: String, done: Boolean = false) derives JsonCodec

trait TodoService:
  def getTodos: UIO[List[Todo]]
  def createTodo(description: String): UIO[Todo]

object TodoService:

  def withTodoService[E, A](call: TodoService => ZIO[TodoService, E, A]) =
    ZIO.serviceWithZIO[TodoService](call)

  def getTodos: URIO[TodoService, List[Todo]] = withTodoService(_.getTodos)
  def createTodo(desc: String): URIO[TodoService, Todo] = withTodoService(_.createTodo(desc))

  class TodoServiceImpl(todosRef: Ref[List[Todo]]) extends TodoService:

    def getTodos: UIO[List[Todo]] =
      for {
        todos <- todosRef.get
      } yield todos.filter(!_.done)

    def createTodo(description: String): UIO[Todo] =
      for {
        todo <- todosRef.modify(todos =>
          val newTodo = Todo(todos.length + 1, description)
          (newTodo, todos :+ newTodo)
        )
      } yield todo

  def live: ZLayer[Any, Nothing, TodoServiceImpl] =
    ZLayer {
      for
        todosRef <- Ref.make(List.empty[Todo])
      yield new TodoServiceImpl(todosRef)
    }
