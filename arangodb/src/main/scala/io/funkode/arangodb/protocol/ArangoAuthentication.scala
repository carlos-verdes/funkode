package io.funkode.arangodb.protocol

enum ArangoAuthentication:
  case None extends ArangoAuthentication
  case UserPassword(encryption: String, user: String, pswd: String) extends ArangoAuthentication
  case Token(encryption: String, token: String) extends ArangoAuthentication

object ArangoAuthentication:

  @SuppressWarnings(Array("stryker4s.mutation.StringLiteral"))
  val PLAIN = "plain"

  @SuppressWarnings(Array("stryker4s.mutation.StringLiteral"))
  val JWT = "jwt"

  def loginMessage(user: String, password: String): ArangoMessage[UserPassword] =
    ArangoMessage(MessageType.Authentication, UserPassword(PLAIN, user, password))

  def loginMessage(token: String): ArangoMessage[Token] =
    ArangoMessage(MessageType.Authentication, Token(JWT, token))
