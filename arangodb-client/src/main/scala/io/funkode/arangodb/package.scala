package io.funkode.arangodb

type ArangoResponse[F[_, _], A] = F[models.ArangoError, A]
