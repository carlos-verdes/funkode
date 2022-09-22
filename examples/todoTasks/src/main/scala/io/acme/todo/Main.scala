package io.acme.todo

import zio.*
import zio.Console.*

object TodoApp extends ZIOAppDefault:

  import TodoService.*
  import outbound.*

  val EXIT = "exit"

  def app: RIO[TodoService, Boolean] = for {
    todos <- getTodos
    _ <- printLine(s"We have ${todos.length} pending todos" + todos.map("\n" + _.description).mkString)
    _ <- printLine(s"""Write next todo (type "$EXIT" so finish):""")
    todoDescription <- readLine
    shouldExit <-
      if (todoDescription.trim.isEmpty || todoDescription.trim.toLowerCase == EXIT)
        ZIO.succeed(false)
      else
        createTodo(todoDescription) *> app
  } yield shouldExit

  def run =
    app.provide(
      TodoService.live,
      TodoRepository.inMemory)
