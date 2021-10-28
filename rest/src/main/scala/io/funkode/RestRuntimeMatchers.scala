/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */

package io.funkode

import scala.reflect.ClassTag

import org.http4s.{Response, Status}
import org.specs2.execute.{Error, Result, Results, Success}
import org.specs2.matcher.{Expectable, MatchResult, Matcher, Matchers, RunTimedMatchers, ValueCheck}

trait RestRuntimeMatchers[F[_]] extends RunTimedMatchers[F] with Matchers {

  // based on https://github.com/etorreborre/specs2-http4s/blob/master/src/main/scala/org/specs2/matcher/Http4sMatchers.scala
  // I don't import the library to avoid compatibility issues with main libraries

  def haveStatus(expected: Status): Matcher[Response[F]] =
    be_===(expected) ^^ { r: Response[F] => r.status.aka("the response status") }

  def returnStatus(s: Status): Matcher[F[Response[F]]] =
    returnValue(haveStatus(s)) ^^ { (m: F[Response[F]]) => m.aka("the returned response status") }

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
