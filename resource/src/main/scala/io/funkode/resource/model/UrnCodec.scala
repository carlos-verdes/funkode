package io.funkode.resource.model

import io.lemonlabs.uri.Urn
import zio.json.*

given urnCodec: JsonCodec[Urn] = JsonCodec(
  JsonEncoder[String].contramap(_.toString),
  JsonDecoder[String].map(Urn.parse)
)
