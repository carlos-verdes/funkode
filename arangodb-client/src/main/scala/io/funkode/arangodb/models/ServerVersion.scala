/*
 * TODO: License goes here!
 */
package io.funkode.arangodb.models

final case class ServerVersion(
    server: String,
    license: String,
    version: String,
    details: Map[String, String] = Map.empty
)
