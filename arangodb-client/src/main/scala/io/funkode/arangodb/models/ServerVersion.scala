/*
 * TODO: License goes here!
 */
package io.funkode.arangodb.models

case class ServerVersion(
    server: String,
    license: String,
    version: String,
    details: Map[String, String] = Map.empty
)
