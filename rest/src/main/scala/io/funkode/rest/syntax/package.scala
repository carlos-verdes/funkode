/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */
package io.funkode.rest

package object syntax {

  object all extends Http4sFunkodeSyntax with HttpFunkodeSyntax

  object http4s extends Http4sFunkodeSyntax
  object http extends HttpFunkodeSyntax
}
