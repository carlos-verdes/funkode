/*
 * TODO: License goes here!
 */
package io.acme.todo
package outbound


import io.funkode.rest.ApiError
import io.lemonlabs.uri.Urn
import zio.*
import zio.concurrent.ConcurrentMap

trait TodoRepository:
  def saveTodo(urn: Urn, todo: Todo): IO[ApiError, Todo]
  def getAllTodos(): IO[ApiError, List[Todo]]

object TodoRepository:

  class InMemoryTodoRepository(todosMap: ConcurrentMap[Urn,Todo]) extends TodoRepository {
    def saveTodo(urn: Urn, todo: Todo): IO[ApiError, Todo] =
      todosMap.put(urn, todo) *> ZIO.succeed(todo)
    def getAllTodos(): IO[ApiError, List[Todo]] = todosMap.toList.map(_.map(_._2))
  }

  def inMemory: ZLayer[Any, Nothing, TodoRepository] =
    ZLayer {
      for
        todosMap <- ConcurrentMap.empty
      yield new InMemoryTodoRepository(todosMap)
    }
