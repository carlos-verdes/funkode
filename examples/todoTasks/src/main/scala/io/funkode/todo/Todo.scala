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

  class TodoServiceImpl() extends TodoService:

    val todosRef = Ref.make(List.empty[Todo])

    def getTodos: UIO[List[Todo]] =
      for {
        ref <- todosRef
        todos <- ref.get
        _ <- ZIO.attempt(println("this are all todos")).orDie
      } yield todos.filter(!_.done)

    def createTodo(description: String): UIO[Todo] =
      for {
        ref <- todosRef
        todo <- ref.modify(todos =>
          val newTodo = Todo(todos.length, description)
          (newTodo, todos :+ newTodo)
        )
      } yield todo

  def live: ZLayer[Any, Nothing, TodoServiceImpl] =
    ZLayer(ZIO.succeed(new TodoServiceImpl()))
