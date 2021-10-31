/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */

package io.funkode.rest

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.catsSyntaxApplicativeId

object Main extends IOApp {
  def run(args: List[String]) =
    ExitCode.Success.pure[IO]
}
