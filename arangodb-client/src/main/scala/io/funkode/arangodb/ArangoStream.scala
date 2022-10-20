/*
 * TODO: License goes here!
 */
package io.funkode.arangodb

trait ArangoStream[F[_], Decoder[_]]:
  type S[_[_], _]
  def fromQuery[V, T: Decoder](query: ArangoQuery[Decoder]): S[F, T]
  //  def evalMap[T, U](s: S[F, T])(f: T => F[U]): S[F, U]

object ArangoStream:
  type Aux[F[_], S_[_[_], _], Decoder[_]] = ArangoStream[F, Decoder] { type S[F_[_], T] = S_[F_, T] }
