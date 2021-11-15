/*
 * Copyright 2021 io.funkode
 *
 * SPDX-License-Identifier: MIT
 */

package io.funkode.arango

import scala.concurrent.duration._

import com.whisk.docker.{DockerContainer, DockerKit, DockerPortMapping, DockerReadyChecker}


trait DockerArango extends DockerKit {

  import DockerArango._

  val DefaultArangoPort = 8529
  val TestDefaultArangoPort = 18529
  val arangoContainer: DockerContainer = DockerContainer(DOCKER_IMAGE)
      .withPortMapping(DefaultArangoPort -> DockerPortMapping(Some(TestDefaultArangoPort)))
      .withEnv("ARANGO_ROOT_PASSWORD=rootpassword")
      .withReadyChecker(
        DockerReadyChecker
            .HttpResponseCode(DefaultArangoPort, "/", Some(HOST))
            .within(TIMEOUT)
            .looped(LOOPED_ATTEMPS, LOOPED_MILLIS))

  abstract override def dockerContainers: List[DockerContainer] = arangoContainer :: super.dockerContainers
}

object DockerArango {

  val DOCKER_IMAGE = "arangodb/arangodb:3.7.10"
  val DOCKER_ENV = "ARANGO_ROOT_PASSWORD=rootpassword"
  val HOST = "0.0.0.0"
  val TIMEOUT = 100.millis
  val LOOPED_ATTEMPS = 20
  val LOOPED_MILLIS = 1250.millis
}
