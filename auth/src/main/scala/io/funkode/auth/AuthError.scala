package io.funkode.auth

enum AuthError(message: String) extends Throwable:

  override def getMessage: String = message

  case WrongRequest[R](request: R, message: String) extends AuthError(message)
  case Forbidden(message: String, cause: Option[Throwable] = None) extends AuthError(message)
