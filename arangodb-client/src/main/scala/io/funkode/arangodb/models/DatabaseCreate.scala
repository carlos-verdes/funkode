package io.funkode.arangodb.models

case class DatabaseCreate(
    name: String,
    options: Map[String, String] = Map.empty,
    users: Vector[DatabaseCreate.User] = Vector.empty
)

object DatabaseCreate:

  final case class User(
    username: String,
    passwd: Option[String] = None,
    active: Boolean = true,
    //  extra: Option[Any],
  )
