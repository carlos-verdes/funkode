/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */

package io.funkode.arango


package object codecs {

  object all extends AuthCodecs with Http4sCodecs

  object authCodecs extends AuthCodecs
  object http4sCodecs extends Http4sCodecs
}
