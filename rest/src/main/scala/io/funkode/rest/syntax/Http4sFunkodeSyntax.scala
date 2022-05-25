/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */
package io.funkode.rest.syntax

import cats.syntax.option._
import org.http4s.Uri
import org.http4s.headers.LinkValue

trait Http4sFunkodeSyntax {

  implicit class UriFunkodeOps(uri: Uri) {

    def link(rel: Option[String] = None): LinkValue = LinkValue(uri, rel)
    def link(rel: String): LinkValue = link(rel.some)
  }

  implicit class LinkValueFunkodeOps(linkValue: LinkValue) {

    def withRel(newRel: String): LinkValue = linkValue.copy(rel = newRel.some)
  }
}
