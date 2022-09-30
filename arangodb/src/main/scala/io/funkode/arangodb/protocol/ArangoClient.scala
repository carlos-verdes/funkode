/*
 * TODO: License goes here!
 */
package io.funkode.arangodb
package protocol

import zio.*

trait ArangoClient:

  type AIO[A] = IO[ArangoError, ArangoResponse[A]]

  def send[I, O](message: ArangoMessage[I]): AIO[O]

  def login(username: String, password: String): AIO[Nothing] =
    send(ArangoAuthentication.loginMessage(username, password))

  def login(token: String): AIO[Nothing] =
    send(ArangoAuthentication.loginMessage(token))

// def server: ArangoServer[F]

//def database(name: DatabaseName): ArangoDatabase[F]

//def system: ArangoDatabase[F]

//def db: ArangoDatabase[F]

/*
object ArangoClient:


  class HttpJsonArangoClient extends ArangoClient:


    private val LOGIN_PATH = "/_open/auth"


  // def server: ArangoServer[F]
  //def database(name: DatabaseName): ArangoDatabase[F]
  //def system: ArangoDatabase[F]
  //def db: ArangoDatabase[F]
 */
