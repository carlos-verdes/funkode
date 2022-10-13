package io.funkode.arangodb
package models

case class DatabaseCreate(
    name: DatabaseName,
    users: Vector[DatabaseCreate.User] = Vector.empty,
    options: Map[String, String] = Map.empty
)

object DatabaseCreate:

  final case class User(
    username: String,
    passwd: Option[String] = None,
    active: Boolean = true,
    //  extra: Option[Any],
  )
