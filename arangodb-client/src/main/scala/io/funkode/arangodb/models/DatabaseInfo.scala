package io.funkode.arangodb
package models

case class DatabaseInfo(
    name: String,
    id: String,
    path: String,
    isSystem: Boolean)
