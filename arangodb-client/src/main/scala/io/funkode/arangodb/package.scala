package io.funkode.arangodb

import zio.*

import models.*
import protocol.*

type AIO[A] = IO[ArangoError, A]
type RAIO[Encoder[_], Decoder[_], O] = ZIO[ArangoClient[Encoder, Decoder], ArangoError, O]
