package io.funkode.todo

import zio.*
import zio.Console.*

object TodoApp extends ZIOAppDefault:

  import TodoService.*


  val app = for {
    todos <- getTodos
    _ <- printLine(s"We have ${todos.length} pending todos")
    _ <- printLine(s"Write next todo:")
    todoDescription <- readLine
    _ <- createTodo(todoDescription)
    moreTodos <- getTodos
    _ <- printLine(s"We have ${moreTodos.length} pending todos")


  } yield ()

  def run =
    app.provide(TodoService.live)
