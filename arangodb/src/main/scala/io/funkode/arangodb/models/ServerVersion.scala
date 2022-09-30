/*
 * TODO: License goes here!
 */
package io.funkode.arangodb
package models

import zio.json.*

final case class ServerVersion(
    server: String,
    license: String,
    version: String,
    details: Map[String, String] = Map.empty
) derives JsonCodec
