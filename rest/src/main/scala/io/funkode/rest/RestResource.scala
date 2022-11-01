package io.funkode
package rest

import io.lemonlabs.uri.Uri

case class ResourceLink(uri: Uri, rel: String, attributes: Map[String, String] = Map.empty)
type ResourceLinks = Map[String, ResourceLink]

case class RestResource[R](
    uri: Uri,
    body: R,
    resourceType: String,
    etag: String,
    links: ResourceLinks = Map.empty
)
