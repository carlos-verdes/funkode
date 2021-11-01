/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */

package org.specs2.matcher

import scala.reflect.ClassTag

import cats.MonadError
import cats.implicits.toFlatMapOps
import org.http4s.{EntityDecoder, Header, Headers, Message, Response, Status}
import org.specs2.execute.{Error, Result, Results, Success}

trait RestMatchers[F[_]] extends RunTimedMatchers[F] with Matchers {

  // based on https://github.com/etorreborre/specs2-http4s/blob/master/src/main/scala/org/specs2/matcher/Http4sMatchers.scala
  // I don't import the library to avoid compatibility issues with main libraries

  def haveStatus(expected: Status): Matcher[Response[F]] =
    be_===(expected) ^^ { r: Response[F] => r.status.aka("the response status") }

  def returnStatus(s: Status): Matcher[F[Response[F]]] =
    returnValue(haveStatus(s)) ^^ { (m: F[Response[F]]) => m.aka("the returned response status") }

  def haveBody[A](a: ValueCheck[A])(
      implicit F: MonadError[F, Throwable],
      ee: EntityDecoder[F, A]
  ): Matcher[Message[F]] =
    returnValue(a) ^^ { (m: Message[F]) => m.as[A].aka("the message body") }

  def returnBody[A](a: ValueCheck[A])(
      implicit F: MonadError[F, Throwable],
      ee: EntityDecoder[F, A]
  ): Matcher[F[Message[F]]] =
    returnValue(a) ^^ { (m: F[Message[F]]) => m.flatMap(_.as[A]).aka("the returned message body") }

  def haveHeaders(hs: Headers): Matcher[Message[F]] =
    be_===(hs) ^^ { (m: Message[F]) => m.headers.aka("the headers") }

  def returnHeaders(hs: Headers): Matcher[F[Message[F]]] =
    returnValue(haveHeaders(hs)) ^^ { (m: F[Message[F]]) => m.aka("the returned headers")}

  def containHeader[H](hs: H)(implicit H: Header.Select[H]): Matcher[Message[F]] =
    beSome(hs) ^^ { (m: Message[F]) =>
      m.headers.get[H](H).asInstanceOf[Option[H]].aka("the particular header")
    }

  def returnContainingHeader[H](hs: H)(implicit H: Header.Select[H]): Matcher[F[Message[F]]] =
    returnValue(containHeader(hs)) ^^ { (m: F[Message[F]]) =>
      m.aka("the returned particular header")
    }

  // everything after this line is new, used for error testing based on ClassTags
  class ErrorValueCheck[T, E: ClassTag] extends ValueCheck[T] {
    override def check: T => Result = (t: T) => {
      t match {
        case x if x.getClass.isAssignableFrom(implicitly[ClassTag[E]].runtimeClass) => Success("Message", "Expected")
        case other => Error(s"error doesn't match, $other", new Exception("whatever"))
      }
    }

    override def checkNot: T => Result = (t: T) => Results.negate(check(t))
  }

  class ErrorTimedMatcher[T, E: ClassTag](check: ErrorValueCheck[T, E]) extends Matcher[F[T]] {

    override def apply[S <: F[T]](e: Expectable[S]): MatchResult[S] =
      try {

        checkResult(e)(runAwait(e.value))
        result(false, "", s"This code should fail with ${implicitly[ClassTag[E]].runtimeClass}", e)
      } catch {
        case x if x.getClass.isAssignableFrom(implicitly[ClassTag[E]].runtimeClass) =>
          result(true, "Captured proper error", s"Error not expected $x", e)
        case error: Throwable =>
          result(false, "testing this ok", s"Error not expected $error", e)
      }

    private def checkResult[S <: F[T]](e: Expectable[S])(t: T): MatchResult[S] =
      result(check.check(t), e)
  }

  def returnError[T, E: ClassTag]: Matcher[F[T]] = new ErrorTimedMatcher[T, E](new ErrorValueCheck[T, E]())
}
