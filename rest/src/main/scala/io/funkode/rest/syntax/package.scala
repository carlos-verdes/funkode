/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */
package io.funkode.rest

package object syntax {

  import io.funkode.rest.resource.HttpResourceSyntax

  object all extends HttpResourceSyntax

  object resource extends HttpResourceSyntax
}
