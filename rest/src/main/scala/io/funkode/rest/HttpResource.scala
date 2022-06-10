/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */

package io.funkode.rest

import cats.implicits.catsSyntaxOptionId
import cats.Functor
import org.http4s._
import org.http4s.headers.{Link, LinkValue}
import simulacrum.typeclass


object resource {

  val REL_SELF = "self"

  case class HttpResource[R](uri: Uri, body: R, links: Link)

  type HttpResources[R] = Vector[HttpResource[R]]

  @typeclass
  trait ToResource[R] {

    def uri(r: R): Uri
    def asResource(r: R): HttpResource[R] = HttpResource(uri(r), r)
  }

  object HttpResource {

    def selfLink(uri: Uri): LinkValue = LinkValue(uri, REL_SELF.some)

    def apply[R](uri: Uri, body: R): HttpResource[R] = HttpResource(uri, body, Link(selfLink(uri)))

    implicit def functorInstance: Functor[HttpResource] =  new Functor[HttpResource] {
      override def map[A, B](resource: HttpResource[A])(f: A => B): HttpResource[B] =
        resource.copy(body = f(resource.body))
    }
  }
}
